package kad.messages

import kotlinx.serialization.Serializable
import pen.Loggable
import pen.Config
import kad.dht.KGetParameter
import kad.node.KNode

/** Messages used to send to another node requesting content */
@Serializable
class KFindValueMessage (val origin : KNode, val params : KGetParameter) : Message(), Loggable
{
   init
   { log(Config.trigger( "KAD_MSG_CREATE" )) }

   override fun tag () = "KFindValueMessage"
}
