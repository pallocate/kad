package kad

import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.util.NoSuchElementException
import java.util.Timer
import java.util.TimerTask
import kotlinx.serialization.Serializable
import pen.Log
import pen.reeadObject; import pen.writeObject
import pen.LogLevel.INFO
import pen.LogLevel.WARN
import pen.LogLevel.ERROR
import pen.Loggable
import pen.createDir
import pen.Constants
import pen.Constants.JSON_EXTENSION
import pen.Config
import kad.Constants as KadConstants
import kad.dht.KDHT
import kad.dht.KContent
import kad.dht.KStorageEntry
import kad.dht.KGetParameter
import kad.messages.receivers.ReceiverFactory
import kad.node.KNode
import kad.node.KNodeId
import kad.operations.KConnectOperation
import kad.operations.KFindValueOperation
import kad.operations.KKadRefreshOperation
import kad.operations.KStoreOperation
import kad.routing.KRoutingTable
import kad.utils.KSerializableRoutingInfo

/** Primary constructor. */
@Serializable
class KKademliaNode () : Loggable
{
   companion object
   {
      /* Filenames used when reading/writing state. */
      private const val KAD                          = "kad"
      private const val ROUTING_TABLE                = "routingtable"
      private const val NODE                         = "node"
      private const val DHT                          = "dht"

      /** Loads  file. */
      fun loadFromFile (ownerName : String) : KKademliaNode?
      {
         Log.debug( "$ownerName- loading KKademliaNode" )
         var ret : KKademliaNode? = null

         try
         {
            /* Reads some basic info. */
            val dir = storageDir( ownerName ) + Constants.SLASH
            val kadNode = readObject<KKademliaNode>( {serializer()}, dir + KAD )

            if (kadNode is KKademliaNode)
            {
               /* Reads routing table info. */
               val rtInfo = readObject<KRoutingTable>( {KRoutingTable.serializer()}, dir + ROUTING_TABLE + JSON_EXTENSION )

               /* Reads local node */
               val node = readObject<KNode>( {KNode.serializer()}, dir + NODE + JSON_EXTENSION )

               /* Reads DHT */
               val dht = readObject<KDHT>( {KDHT.serializer()}, dir + DHT + JSON_EXTENSION )

               if (rtInfo is KRoutingTable && node is KNode && dht is KDHT)
               {
                  kadNode.initialize( node, rtInfo.toRoutingTable(), dht )
                  ret = kadNode
               }
            }
         }
         catch (e : Exception)
         { Log.error( "${ownerName}- KKademliaNode load failed! ${e.message}" ) }

         return ret
      }

      /** @return The name of the content storage folder. */
      fun storageDir (nameDir : String, subDir : String = "nodeState" ) : String = StringBuilder().apply {
         append( Constants.USER_HOME )
         append( Constants.SLASH )
         append( Constants.CONFIG_DIR )
         append( Constants.SLASH )
         append( "kad" )
         append( Constants.SLASH )
         append( nameDir )
         append( Constants.SLASH )
         append( subDir )
         createDir( toString() )
      }.toString()
   }

   var ownerName                                  = ""
   var port                                       = 0                           // 49152-65535 are private ports
   private var node                               = KNode()
   private var routingTable                       = KRoutingTable()
   private var dht                                = KDHT()

   private var server                             = KServer()
   private var refreshTimer : Timer?              = null
   private var refreshTask                        = RefreshTimerTask()

   init
   {Log.info( "KKademliaNode- created" )}

   /** Secondary constructor. */
   constructor (name : String, id : String, port : Int) : this ()
   {
      ownerName = name
      this.port = port
      node = KNode( KNodeId( id ), InetAddress.getLocalHost(), port )

      initialize()
   }

   internal fun initialize (node : KNode, routingTable : KRoutingTable, dht : KDHT)
   {
      this.routingTable = routingTable
      this.node = node
      this.dht = dht

      initialize()
   }

   private fun initialize ()
   {
      log("initializing", Config.trigger( "KAD_INITIALIZE" ))

      routingTable.initialize( node )
      dht.initialize( ownerName )
      server.initialize( this, port )

      startRefreshing()
   }

   @Synchronized
   fun bootstrap (otherNode : KNode)
   {
      log("bootstrapping to (${otherNode})", Config.trigger( "KAD_BOOTSTRAP" ))
      val startTime = System.nanoTime()*1000
      val op = KConnectOperation( server, node, routingTable, dht, otherNode )

      try
      {
         op.execute()
         log("bootstrap complete", Config.trigger( "KAD_BOOTSTRAP" ))

         val endTime = System.nanoTime()*1000
         Stats.setBootstrapTime( endTime - startTime )
      }
      catch (e: Exception)
      {
         log("connection failed, ${e.message}", Config.trigger( "KAD_BOOTSTRAP" ))
      }
   }

   fun put(content : KContent) = put(KStorageEntry( content ))
   fun put (entry : KStorageEntry) : Int
   {
      log("storing entry [${entry.content.key.shortName()}]", Config.trigger( "KAD_CONTENT_PUT_GET" ))
      val storeOperation = KStoreOperation( server, node, routingTable, dht, entry )
      storeOperation.execute()

      /* Return how many nodes the content was stored on */
      return storeOperation.numNodesStoredAt()
   }

   fun putLocally (content : KContent)
   {
      log("storing entry [${content.key.shortName()}] locally", Config.trigger( "KAD_CONTENT_PUT_GET" ))
      dht.store(KStorageEntry( content ))
   }

   fun get (kGetParameter : KGetParameter) : StorageEntry
   {
      log("retrieving entry [${kGetParameter.key.shortName()}]", Config.trigger( "KAD_CONTENT_PUT_GET" ))
      if (dht.contains( kGetParameter ))
      {
         /* If the content exist in our own KDHT, then return it. */
         return dht.get( kGetParameter )
      }

      /* Seems like it doesn't exist in our KDHT, get it from other Nodes */
      val startTime = System.nanoTime()
      val kFindValueOperation = KFindValueOperation( server, node, routingTable, kGetParameter )
      kFindValueOperation.execute()
      val endTime = System.nanoTime()
      Stats.addContentLookup( endTime - startTime, kFindValueOperation.routeLength(), kFindValueOperation.isContentFound )

      return kFindValueOperation.getContentFound()
   }

   fun refresh ()
   {KKadRefreshOperation( server, node, routingTable, dht ).execute()}

  /* @param saveState If this  should be saved. */
   fun shutdown (saveState : Boolean)
   {
      Log.info( "${ownerName}- shutting down!")
      server.shutdown()
      stopRefreshing()

      if (saveState)
         saveState()
   }

   private fun saveState ()
   {
      log("saving", Config.trigger( "KAD_SAVE_LOAD" ))
      val dir = storageDir( ownerName ) + Constants.SLASH

      /* Store Basic  data. */
      writeObject( this, dir + KAD + JSON_EXTENSION )

      /* Save the node state. */
      writeObject( node, dir + NODE + JSON_EXTENSION )

      /* Save the routing table. */
      writeObject( KSerializableRoutingInfo( routingTable ), dir + ROUTING_TABLE + JSON_EXTENSION )

      /* Save the DHT. */
      writeObject( dht, dir + DHT + JSON_EXTENSION )
   }

   private fun startRefreshing ()
   {
      log("start refreshing", Config.trigger( "KAD_INTERNAL" ))
      refreshTimer = Timer( true )
      refreshTask = RefreshTimerTask()
      refreshTimer?.schedule( refreshTask, KadConstants.RESTORE_INTERVAL, KadConstants.RESTORE_INTERVAL )
   }

   private fun stopRefreshing ()
   {
      log("stop refreshing", Config.trigger( "KAD_INTERNAL" ))
      refreshTask.cancel()
      refreshTimer?.cancel()
      refreshTimer?.purge()
   }

   override fun toString () : String
   {
      val sb = StringBuilder( "\n\n owner: $ownerName \n\n\n" )

      sb.append("Local node: ${ node.nodeId }\n\n")
      sb.append( "Routing Table: $routingTable\n\n" )
      sb.append( "KDHT: $dht\n\n" )

      return sb.toString()
   }

   override fun originName () = "KKademliaNode(${node})"

   fun getRoutingTable () = routingTable
   fun getNode () = node
   fun getDHT () = dht
   fun getServer () = server

   inner class RefreshTimerTask () : TimerTask()
   {
      override fun run ()
      {
         try
         { refresh() }
         catch (e : IOException)
         { log("refresh failed!", Config.trigger( "KAD_INTERNAL" ), WARN) }
      }

      override fun cancel () : Boolean
      {
         log("refresh canceled", Config.trigger( "KAD_INTERNAL" ))
         return false
      }
   }
}
