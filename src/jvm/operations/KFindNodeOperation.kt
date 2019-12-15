package kad.operations

import kotlinx.coroutines.*
import java.io.IOException
import java.util.ArrayList
import java.util.TreeMap
import pen.Loggable
import kad.Constants
import kad.KServer
import kad.KKademliaNode
import kad.RoutingException
import kad.messages.Message
import kad.messages.receivers.Receiver
import kad.messages.Codes
import kad.messages.KFindNodeMessage
import kad.messages.KFindNodeReply
import kad.node.KKeyComparator
import kad.node.KNode
import kad.node.KNodeId
import kad.routing.KRoutingTable

/** Finds the K closest nodes to a specified identifier
  * The algorithm terminates when it has gotten responses from the K closest nodes it has seen.
  * Nodes that fail to respond are removed from consideration */
class KFindNodeOperation
/** @param server Server used for communication
  * @param kadNode The local node making the communication
  * @param lookupId  The ID for which to find nodes close to */
(private val server : KServer, private val node : KNode, private val routingTable : KRoutingTable, private val lookupId : KNodeId) : Operation, Receiver, Loggable
{
   /* Constants */
   private val UNASKED  = "UNASKED"
   private val AWAITING = "AWAITING"
   private val ASKED    = "ASKED"
   private val FAILED   = "FAILED"

   private val messagesTransiting = HashMap<Int, KNode>()                       // Tracks messages in transit and awaiting reply
   private val lookupMessage = KFindNodeMessage( node, lookupId )               // Message sent to each peer
   private val comparator = KKeyComparator( lookupId )
   private val nodes = TreeMap<KNode, String>( comparator )                     // Will be sorted by which nodes are closest to the lookupId

   override fun execute ()
   { runBlocking { nodeLookup() }}

   /* This wont be not thread safe(but fast!) */
   suspend fun nodeLookup ()
   {
      /* Set the local node as already asked */
      nodes.put( node, ASKED )

      /* We add all nodes here instead of the K-Closest because there may be the case that the K-Closest are offline
        * - The operation takes care of looking at the K-Closest.*/
      addNodes( routingTable.allNodes() )

      /* If we haven't finished as yet, wait for a maximum of Constants.OPERATION_TIMEOUT time */
      var totalTimeWaited = 0
      val timeInterval = 10                                                     // We re-check every n milliseconds

      while (totalTimeWaited < Constants.OPERATION_TIMEOUT)
      {
         if (!askNodesorFinish())
         {
            delay( timeInterval.toLong() )
            totalTimeWaited += timeInterval
         }
         else
            break
      }

      /* Now after we've finished, we would have an idea of offline nodes, lets update our routing table */
      routingTable.setUnresponsiveContacts( getFailedNodes() )
   }

   fun getClosestNodes () = closestNodes( ASKED )
   fun getFailedNodes () : ArrayList<KNode>
   {
      val failedNodes = ArrayList<KNode>()

      for (e in nodes.entries)
         if (e.value.equals( FAILED ))
            failedNodes.add(e.key)

      return failedNodes
   }

   /** Add nodes from this list to the set of nodes to lookup
     * @param list The list from which to add nodes */
   fun addNodes (list : List<KNode>)
   {
      for (o in list)
      {
         if (!nodes.containsKey( o ))
            nodes.put( o, UNASKED )
      }
   }

   /** Asks some of the K closest nodes seen but not yet queried.
     * Assures that no more than DefaultConfig.CONCURRENCY messages are in transit at a time
     *
     * This method should be called every time a reply is received or a timeout occurs.
     * If all K closest nodes have been asked and there are no messages in transit,
     * the algorithm is finished.
     * @return `true` if finished OR `false` otherwise */
   private fun askNodesorFinish () : Boolean
   {
      /* If >= CONCURRENCY nodes are in transit, don't do anything */
      if (Constants.MAX_CONCURRENT_MESSAGES_TRANSITING <= messagesTransiting.size)
         return false

      /* Get unqueried nodes among the K closest seen that have not FAILED */
      val unasked = closestNodesNotFailed( UNASKED )

      if (unasked.isEmpty() && messagesTransiting.isEmpty())
         /* We have no unasked nodes nor any messages in transit, we're finished! */
         return true

      /* Send messages to nodes in the list
       * making sure than no more than CONCURRENCY messsages are in transit */
      var i = 0
      while (messagesTransiting.size < Constants.MAX_CONCURRENT_MESSAGES_TRANSITING && i < unasked.size)
      {
         val n = unasked[i]
         val comm = server.sendMessage(n, lookupMessage, this)

         nodes.put( n, AWAITING )
         messagesTransiting.put( comm, n )
         i++
      }

      /* We're not finished as yet, return false */
      return false
   }

   /** @param status The status of the nodes to return
     * @return The K closest nodes to the target lookupId given that have the specified status */
   private fun closestNodes (status : String) : MutableList<KNode>
   {
      val closestNodes = ArrayList<KNode>( Constants.K )
      var remainingSpaces = Constants.K

      for (e in nodes.entries)
         if (status.equals( e.value ))
         {
            /* We got one with the required status, now add it */
            closestNodes.add( e.key )
            if (--remainingSpaces == 0)
               break
         }

      return closestNodes
   }

   /** Find The K closest nodes to the target lookupId given that have not FAILED.
     * From those K, get those that have the specified status
     * @param status The status of the nodes to return
     * @return A List of the closest nodes */
   private fun closestNodesNotFailed (status : String) : MutableList<KNode>
   {
      val closestNodes = ArrayList<KNode>( Constants.K )
      var remainingSpaces = Constants.K

      for (e in nodes.entries)
         if (!FAILED.equals( e.value ))
         {
            if (status.equals( e.value ))
               /* We got one with the required status, now add it */
               closestNodes.add( e.key )

            if (--remainingSpaces == 0)
               break
         }

      return closestNodes
   }

   /** Receive and handle the incoming KFindNodeReply */
   @Synchronized
   override fun receive (message : Message, conversationId : Int)
   {
      if (message !is KFindNodeReply) // Not sure why we get a message of a different type here... @todo Figure it out..
         return

      /* We receive a KFindNodeReply with a set of nodes, read this message */
      val msg = message

      /* Add the origin node to our routing table */
      val origin = msg.origin
      routingTable.insert( origin )

      /* Set that we've completed ASKing the origin node */
      nodes.put( origin, ASKED )

      /* Remove this msg from messagesTransiting since it's completed now */
      messagesTransiting.remove( conversationId )

      /* Add the received nodes to our nodes list to query */
      addNodes( msg.nodes )
      askNodesorFinish()
   }

   /** A node does not respond or a packet was lost, we set this node as failed */
   @Synchronized
   override fun timeout (conversationId : Int)
   {
      /* Get the node associated with this communication */
      val n = messagesTransiting[conversationId] ?: return

      /* Mark this node as failed and inform the routing table that it is unresponsive */
      nodes.put( n, FAILED )
      routingTable.setUnresponsiveContact( n )
      messagesTransiting.remove( conversationId )

      askNodesorFinish()
   }

   override fun originName () = "KFindNodeOperation(${node})"
}
