package kad.operations

import kad.KServer
import kad.KKademliaNode
import kad.messages.Message
import kad.dht.KDHT
import kad.dht.KStorageEntry
import kad.StorageEntry
import kad.messages.receivers.NoReceiver
import kad.messages.KStoreMessage
import kad.node.KNode
import kad.routing.KRoutingTable

/** Operation that stores a DHT KContent onto the K closest nodes to the content Key
  * @param storageEntry The content to be stored on the DHT
  * @param dht The local DHT */
class KStoreOperation (private val server : KServer, private val node : KNode, private val routingTable : KRoutingTable, private val dht : KDHT, private val storageEntry : KStorageEntry) : Operation
{
   @Synchronized
   override fun execute ()
   {
      /* Get the nodes on which we need to store the content */
      val kFindNodeOperation = KFindNodeOperation( server, node, routingTable, storageEntry.contentMetadata.nodeId )
      kFindNodeOperation.execute()
      val nodes = kFindNodeOperation.getClosestNodes()

      /* Create the message */
      val msg = KStoreMessage( node, storageEntry )

      /*KStoreMessage the message on all of the K-Nodes*/
      for (n in nodes)
      {
         if (n.equals( node ))
         dht.store( storageEntry )                                         // KStoreMessage the content locally
         else
         /** @todo Create a receiver that receives a store acknowledgement message to count how many nodes a content have been stored at */
         server.sendMessage( n, msg, NoReceiver() )
      }
   }

   /** @return The number of nodes that have stored this content
     * @todo Implement this method */
   fun numNodesStoredAt () = 1
}
