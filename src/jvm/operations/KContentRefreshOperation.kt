package kad.operations

import java.io.IOException
import pen.Loggable
import pen.LogLevel.WARN
import pen.LogLevel.ERROR

import pen.Config
import kad.Constants
import kad.KServer
import kad.KKademliaNode
import kad.messages.Message
import kad.dht.KDHT
import kad.dht.KStorageEntry
import kad.messages.receivers.NoReceiver
import kad.messages.KStoreMessage
import kad.node.KNode
import kad.routing.KRoutingTable

/** Refresh/Restore the data on this node by sending the data to the K-Closest nodes to the data */
class KContentRefreshOperation (private val server : KServer, private val node : KNode, private val routingTable : KRoutingTable, private val dht : KDHT) : Operation, Loggable
{
   /** For each content stored on this DHT, distribute it to the K closest nodes
     * Also delete the content if this node is no longer one of the K closest nodes
     *
     * We assume that our JKademliaRoutingTable is updated, and we can get the K closest nodes from that table */
   @Throws( IOException::class )
   override fun execute ()
   {
      /* Get a list of all storage entries for content */
      val entries = dht.getStorageEntries()

      /* If a content was last republished before this time, then we need to republish it */
      val minRepublishTime = System.currentTimeMillis()/1000L - Constants.RESTORE_INTERVAL

      /* For each storage entry, distribute it */
      for (e in entries)
      {
         /* Check last update time of this entry and only distribute it if it has been last updated > 1 hour ago */
         if (e.lastRepublished > minRepublishTime)
            continue

         /* Set that this content is now republished */
         e.updateLastRepublished()

         /* Get the K closest nodes to this entries */
         val closestNodes = routingTable.findClosest( e.key, Constants.K )

         /* Create the message */
         val storageEntry = dht.get( e )
         if (storageEntry is KStorageEntry)
         {
            val msg = KStoreMessage( node, storageEntry )

            /*KStoreMessage the message on all of the K-Nodes*/
            for (n in closestNodes)
            {
               /*We don't need to again store the content locally, it's already here*/
               if (!n.equals( node ))                          // Send a contentstore operation to the K-Closest nodes
                  server.sendMessage( n, msg, NoReceiver() )
            }

            /* Delete any content on this node that this node is not one of the K-Closest nodes to */
            try
            {
               if (!closestNodes.contains( node ))
                  dht.remove( e )
            }
            catch (e : Exception)
            {
               /* It would be weird if the content is not found here */
               log("KContentRefreshOperation(${node})- remove local content failed!", Config.trigger( "KAD_CONTENT_PUT_GET" ), ERROR)
            }
         }
         else
            log({ "KContentRefreshOperation(${node})- entry not found!"}, Config.trigger( "KAD_CONTENT_PUT_GET" ), WARN)
      }
   }
   override fun originName () = "KContentRefreshOperation"
}
