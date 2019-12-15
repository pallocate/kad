package kad.messages

import pen.Loggable

import pen.Config
import kad.StorageEntry
import kad.dht.KStorageEntry
import kad.node.KNode

/** A KStoreMessage used to send a store message to a node */
class KStoreMessage () : Message, Loggable
{
   var origin                    = KNode()
   var payload                   = KStorageEntry()

   init
   { log( {"<STORE>"}, Config.trigger( "KAD_MSG_CREATE" )) }

   constructor (origin : KNode, payload : KStorageEntry) : this()
   {
      this.origin = origin
      this.payload = payload
   }

   override fun code () = Codes.STORE
   override fun originName () = "KStoreMessage"
}
