package kad

import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.util.NoSuchElementException
import java.util.Timer
import java.util.TimerTask
import pen.Log
import pen.deserializeFromFile
import pen.serializeToFile
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
import kad.messages.receivers.KReceiverFactory
import kad.node.Node
import kad.node.NoNode
import kad.node.KNode
import kad.node.KNodeId
import kad.operations.KConnectOperation
import kad.operations.KFindValueOperation
import kad.operations.KKadRefreshOperation
import kad.operations.KStoreOperation
import kad.routing.KRoutingTable


/** Primary constructor. */
class KKademliaNode () : Loggable
{
   companion object
   {
      /* Filenames used when reading/writing state. */
      private const val NODE                           = "node"
      private const val ROUTING_TABLE                  = "routingtable"
      private const val DHT                            = "dht"

      /** Loads  file. */
      fun loadFromFile (ownerName : String) : KKademliaNode?
      {
         Log.debug( "$ownerName- loading KKademliaNode" )
         var ret = KKademliaNode()

         try
         {
            /* Reads some basic info. */
            val dir = storageDir( ownerName ) + Constants.SLASH

            /* Reads node */
            val kNode = deserializeFromFile<KNode>( dir + NODE + JSON_EXTENSION, KNode.serializer() )
            ret.node = kNode ?: KNode()

            /* Reads routing table info. */
            val kRoutingTable = deserializeFromFile<KRoutingTable>( dir + ROUTING_TABLE + JSON_EXTENSION, KRoutingTable.serializer() )
            ret.routingTable = kRoutingTable ?: KRoutingTable()

            /* Reads DHT */
            val kDHT = deserializeFromFile<KDHT>( dir + DHT + JSON_EXTENSION, KDHT.serializer() )
            ret.dht = kDHT ?: KDHT()
         }
         catch (e : Exception)
         { Log.error( "${ownerName}- KKademliaNode load failed! ${e.message}" ) }

         ret.initialize()

         return ret
      }

      /** @return The name of the content storage folder. */
      fun storageDir (nameDir : String, subDir : String = "nodeState" ) : String = StringBuilder().apply {
         append( KadConstants.USER_HOME )
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

   private var node : Node                             = NoNode()

   private var routingTable                            = KRoutingTable()
   private var dht                                     = KDHT()
   val server                                          = KServer()
//      private set

   private var refreshTimer : Timer?                   = null
   private var refreshTask                             = RefreshTimerTask()

   init
   {Log.info( "KKademliaNode- created" )}

   /** Secondary constructor. */
   constructor (name : String, node : KNode = KNode()) : this ()
   {
      dht.ownerName = name
      this.node = node
      initialize()
   }

   fun initialize ()
   {
      log("initializing", Config.trigger( "KAD_INITIALIZE" ))

      routingTable.initialize( getNode() )
      dht.initialize()
      server.initialize( KReceiverFactory( this ), getNode().port )

      startRefreshing()
   }

   @Synchronized
   fun bootstrap (otherNode : KNode)
   {
      log("bootstrapping to (${otherNode})", Config.trigger( "KAD_BOOTSTRAP" ))
      val startTime = System.nanoTime()*1000
      val op = KConnectOperation( server, getNode(), routingTable, dht, otherNode )

      try
      {
         op.execute()
         log("bootstrap complete", Config.trigger( "KAD_BOOTSTRAP" ))

         val endTime = System.nanoTime()*1000
         server.stats.setBootstrapTime( endTime - startTime )
      }
      catch (e: Exception)
      {
         log("connection failed, ${e.message}", Config.trigger( "KAD_BOOTSTRAP" ))
      }
   }

   fun put(content : KContent) = put(KStorageEntry( content ))
   fun put (entry : KStorageEntry) : Int
   {
      log("storing entry [${entry.content.nodeId.shortName()}]", Config.trigger( "KAD_CONTENT_PUT_GET" ))
      val storeOperation = KStoreOperation( server, getNode(), routingTable, dht, entry )
      storeOperation.execute()

      /* Return how many nodes the content was stored on */
      return storeOperation.numNodesStoredAt()
   }

   fun putLocally (content : KContent)
   {
      log("storing entry [${content.nodeId.shortName()}] locally", Config.trigger( "KAD_CONTENT_PUT_GET" ))
      dht.store(KStorageEntry( content ))
   }

   fun get (kGetParameter : KGetParameter) : StorageEntry
   {
      log("retrieving entry [${kGetParameter.nodeId.shortName()}]", Config.trigger( "KAD_CONTENT_PUT_GET" ))
      if (dht.contains( kGetParameter ))
      {
         /* If the content exist in our own KDHT, then return it. */
         return dht.get( kGetParameter )
      }

      /* Seems like it doesn't exist in our KDHT, get it from other Nodes */
      val startTime = System.nanoTime()
      val kFindValueOperation = KFindValueOperation( server, getNode(), routingTable, kGetParameter )
      kFindValueOperation.execute()
      val endTime = System.nanoTime()
      server.stats.contentLookup( endTime - startTime, kFindValueOperation.routeLength(), kFindValueOperation.isContentFound )

      return kFindValueOperation.getContentFound()
   }

   fun refresh ()
   {KKadRefreshOperation( server, getNode(), routingTable, dht ).execute()}

  /* @param saveState If this  should be saved. */
   fun shutdown (saveState : Boolean)
   {
      Log.info( "${dht.ownerName}- shutting down!")
      server.shutdown()
      stopRefreshing()

      if (saveState)
         saveState()
   }

   override fun toString () : String
   {
      val sb = StringBuilder()

      sb.append( "owner: ${dht.ownerName}\n" )
      if (node is KNode)
      {
         val n = node as KNode
         sb.append( "tag: ${n.nodeId.shortName()}\n" )
         sb.append( "address: ${InetAddress.getByAddress( n.address ).getHostAddress()}\n" )
         sb.append( "port: ${n.port}\n" )
      }
      sb.append( "server running: ${server.isRunning}\n" )

      return sb.toString()
   }

   fun ownerName () = dht.ownerName

   override fun tag () = "KKademliaNode(${node})"

   fun getRoutingTable () = routingTable
   fun getDHT () = dht
   fun getNode () : KNode
   {
      if (node is NoNode)
         node = KNode()

      return node as KNode
   }

   private fun saveState ()
   {
      log("saving", Config.trigger( "KAD_SAVE_LOAD" ))
      val dir = storageDir( dht.ownerName ) + Constants.SLASH

      /* Store Basic data. */
      serializeToFile( getNode(), dir + NODE + JSON_EXTENSION, KNode.serializer() )

      /* Save the routing table. */
      serializeToFile<KRoutingTable>( routingTable, dir + ROUTING_TABLE + JSON_EXTENSION, KRoutingTable.serializer() )

      /* Save the DHT. */
      serializeToFile<KDHT>( dht, dir + DHT + JSON_EXTENSION, KDHT.serializer() )
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
