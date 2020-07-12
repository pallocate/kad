package kad.messages

import kotlinx.serialization.Serializable
import pen.Loggable
import pen.Config
import kad.StorageEntry
import kad.dht.KStorageEntry
import kad.node.KNode

/** A KStoreMessage used to send a store message to a node */
@Serializable
class KStoreMessage (val origin : KNode, val payload : KStorageEntry) : Message(), Loggable
{
   init
   { log(Config.trigger( "KAD_MSG_CREATE" )) }

   override fun tag () = "KStoreMessage"
}
