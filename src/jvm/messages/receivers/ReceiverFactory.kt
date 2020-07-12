package kad.messages.receivers

import pen.Config
import kad.KKademliaNode
import kad.messages.*

interface ReceiverFactory
{ fun createReceiver (message : Message) : Receiver }
class NoReceiverFactory : ReceiverFactory
{ override fun createReceiver (message : Message) = NoReceiver() }

class KReceiverFactory (var localNode : KKademliaNode) : ReceiverFactory
{
   override fun createReceiver (message : Message) : Receiver
   {
      var ret : Receiver = NoReceiver()

      val server = localNode.server
      val node = localNode.getNode()
      val routingTable = localNode.getRoutingTable()
      val dht = localNode.getDHT()

      ret = when (message)
      {
         is KConnectMessage               -> KConnectReceiver( server, node, routingTable )
         is KFindValueMessage             -> KFindValueReceiver( server, node, routingTable, dht )
         is KFindNodeMessage              -> KFindNodeReceiver( server, node, routingTable )
         is KStoreMessage                 -> KStoreReceiver( server, routingTable, dht )
         else                             -> KSimpleReceiver()
      }

      return ret
   }
}
