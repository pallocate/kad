package kad.messages

import kotlinx.serialization.Serializable
import pen.Loggable
import pen.Config
import kad.node.KNode

/** A message used to connect nodes.
  * When a NodeLookup Request comes in, we respond with a KFindNodeReply */
@Serializable
class KFindNodeReply (val origin : KNode, val nodes : ArrayList<KNode>) : Message(), Loggable
{
   init
   { log(Config.trigger( "KAD_MSG_FIND_NODE" )) }

   override fun tag () = "KFindNodeReply"
}
