package kad.node

import java.net.InetAddress
import java.net.InetSocketAddress
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import pen.Log

interface Node
class NoNode : Node

/** A Node in the Kademlia network - Contains basic node network information.
  * @param address The IP address of this node. */
@Serializable
class KNode (val nodeId : KNodeId = KNodeId(), val port : Int = 42283, val address : ByteArray = byteArrayOf(127, 0, 1, 1)) : Node // 49152-65535 are private ports
{
   fun getSocketAddress () = InetSocketAddress(InetAddress.getByAddress( address ), port)

   override fun equals (other : Any?) : Boolean
   {
      var ret = false
      if (other is KNode)
      {
         if (other === this)
            ret = true
         else
            ret = nodeId.equals( other.nodeId )
      }

      return ret
   }

   override fun hashCode() = nodeId.hashCode()
   override fun toString () = nodeId.shortName()
}
