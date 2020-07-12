package kad.messages

import kotlinx.serialization.Serializable
import pen.Loggable
import pen.Config
import kad.node.KNode
import kad.node.KNodeId

/** A message sent to other nodes requesting the K-Closest nodes to a key sent in this message
     * @param origin The KNode from which the message is coming from
     * @param lookup The key for which to lookup nodes for */
@Serializable
class KFindNodeMessage (val origin : KNode, val lookupId : KNodeId) : Message(), Loggable
{
   init
   { log(Config.trigger( "KAD_MSG_FIND_NODE" )) }

   override fun tag () = "KFindNodeMessage"
}
