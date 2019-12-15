package kad.messages

import pen.Loggable

import pen.Config
import kad.dht.KStorageEntry
import kad.node.KNode

/** A Message used to send content between nodes */
class KContentMessage (): Message, Loggable
{
   var origin                    = KNode()
   var content                   = KStorageEntry()

   init
   { log( {"<CONTENT>"}, Config.trigger( "KAD_MSG_CREATE" )) }

   constructor (origin : KNode, content : KStorageEntry) : this()
   {
      this.origin = origin
      this.content = content
   }

   override fun code () = Codes.CONTENT
   override fun originName () = "KContentMessage"
}
