package kad.messages.receivers

import kad.KServer
import kad.messages.Message
import kad.messages.KConnectMessage
import kad.messages.KAcknowledgeMessage
import kad.node.KNode
import kad.routing.KRoutingTable

/** Receives a ConnectMessage and sends an AcknowledgeMessage as reply. */
class KConnectReceiver (private val server : KServer, private val kNode : KNode, private val kRoutingTable : KRoutingTable) : Receiver
{
   /** Handle receiving a ConnectMessage */
   override fun receive (message : Message, conversationId : Int)
   {
      if (message is KConnectMessage)
      {
         /* Update the local space by inserting the origin node. */
         kRoutingTable.insert( message.origin )

         /* Respond to the connect request */
         val msg = KAcknowledgeMessage( kNode )
         server.reply( message.origin, msg, conversationId )
      }
   }
}
