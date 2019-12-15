package kad.operations

import kotlinx.coroutines.*
import pen.Config
import kad.KServer
import kad.KKademliaNode
import kad.dht.KDHT
import kad.node.KNode
import kad.node.KNodeId
import kad.routing.KRoutingTable

/** At each time interval t, nodes need to refresh their K-Buckets
  * This operation takes care of refreshing this node's K-Buckets */
class KBucketRefreshOperation (private val server : KServer, private val node : KNode, private val routingTable : KRoutingTable, private val dht : KDHT) : Operation
{
   /** Each bucket need to be refreshed at every time interval t.
     * Find an identifier in each bucket's range, use it to look for nodes closest to this identifier
     * allowing the bucket to be refreshed.
     *
     * Then Do a FindNodeOperation for each of the generated NodeIds,
     * This will find the K-Closest nodes to that ID, and update the necessary K-Bucket */
   @Synchronized
   override fun execute ()
   {
      GlobalScope.launch { coroutineScope {                                     // Cleans up garbage and awaits returns
         for (i in 1 until KNodeId.ID_SIZE)
         {
            /* Construct a KNodeId that is i bits away from the current node Id */
            val current = node.nodeId.generateNodeIdByDistance( i )

            /* Run the Node Lookup Operation, using coroutines to speed things up. */
            val kFindNodeOperation = KFindNodeOperation( server, node, routingTable, current )
            kFindNodeOperation.nodeLookup()
         }
      }}
   }
}
