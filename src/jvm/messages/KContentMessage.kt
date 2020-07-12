package kad.messages

import kotlinx.serialization.Serializable
import pen.Loggable
import pen.Config
import kad.dht.KStorageEntry
import kad.node.Node
import kad.node.NoNode
import kad.node.KNode

/** A Message used to send content between nodes */
@Serializable
class KContentMessage (val origin : KNode, val content : KStorageEntry): Message(), Loggable
{
   init
   { log(Config.trigger( "KAD_MSG_CREATE" )) }

   override fun tag () = "KContentMessage"
}
