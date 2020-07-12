package kad.messages

import kotlinx.serialization.Serializable
import pen.Loggable
import pen.Config
import kad.node.KNode

/** A message sent to another node requesting to connect to them */
@Serializable
class KConnectMessage (val origin : KNode) : Message(), Loggable
{
   init
   { log(Config.trigger( "KAD_MSG_CREATE" )) }

   override fun tag () = "KConnectMessage"
}
