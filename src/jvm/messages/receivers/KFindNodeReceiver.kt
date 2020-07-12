package kad.messages.receivers

import kad.Constants
import kad.KServer
import kad.messages.Message
import kad.messages.KFindNodeMessage
import kad.messages.KFindNodeReply
import kad.node.KNode
import kad.routing.KRoutingTable

/** Receives a KFindNodeMessage and sends a KFindNodeReply as reply with the K-Closest nodes to the ID sent. */
class KFindNodeReceiver (private val server : KServer, private val kNode : KNode, private val kRoutingTable : KRoutingTable) : Receiver
{
   /** Handle receiving a KFindNodeMessage
     * Find the set of K nodes closest to the lookup ID and return them */
   override fun receive (message : Message, conversationId : Int)
   {
      if (message is KFindNodeMessage)
      {
         /* Update the local space by inserting the origin node. */
         kRoutingTable.insert( message.origin )

         /* Find nodes closest to the LookupId */
         val nodes = kRoutingTable.findClosest( message.lookupId, Constants.K )

         /* Respond to the KFindNodeMessage */
         val reply = KFindNodeReply( kNode, nodes )

         if (server.isRunning)
         {
            /* Let the Server send the reply */
            server.reply( message.origin, reply, conversationId )
         }
      }
   }
}
