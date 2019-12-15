package kad.node

import java.io.Serializable
import java.net.InetAddress
import java.net.InetSocketAddress
import pen.Log
import pen.Filable

/** A Node in the Kademlia network - Contains basic node network information. */
class KNode () : Filable
{
   var nodeId                                     = KNodeId()
   var inetAddress                                = InetAddress.getLocalHost()
   var port                                       = 0

   constructor (nodeId : KNodeId, inetAddress : InetAddress, port : Int) : this()
   {
      this.nodeId = nodeId
      this.inetAddress = inetAddress
      this.port = port
   }

   /** Creates a SocketAddress for this node */
   fun getSocketAddress () = InetSocketAddress( inetAddress, port )

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
