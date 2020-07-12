package kad.operations

import kad.KServer
import kad.node.KNode

/** Implementation of the Kademlia Ping operation,
  * This is on hold at the moment since I'm not sure if we'll use ping given the improvements mentioned in the paper.
  * @param server The Kademlia server used to send & receive messages
  * @param local  The local node
  * @param toPing The node to send the ping message to */
class KPingOperation (private val server : KServer, private val node : KNode, private val toPing : KNode) : Operation
{
    override fun execute()
    {
        throw UnsupportedOperationException( "Not supported yet." )             //To change body of generated methods, choose Tools | Templates.
    }
}
