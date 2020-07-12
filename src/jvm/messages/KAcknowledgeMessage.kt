package kad.messages

import kotlinx.serialization.Serializable
import pen.Loggable
import pen.Config
import kad.node.KNode

/** A message used to acknowledge a request from a node; can be used in many situations.
  * - Mainly used to acknowledge a connect message */
@Serializable
class KAcknowledgeMessage (val origin : KNode) : Message(), Loggable
{
   init
   { log(Config.trigger( "KAD_MSG_CREATE" )) }

   override fun tag () = "KAcknowledgeMessage"
}
