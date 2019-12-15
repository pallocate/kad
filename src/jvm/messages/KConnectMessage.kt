package kad.messages

import pen.Loggable
import pen.Config
import kad.node.KNode

/** A message sent to another node requesting to connect to them */
class KConnectMessage () : Message, Loggable
{
   var origin : KNode = KNode()

   init
   { log( {"<CONNECT>"}, Config.trigger( "KAD_MSG_CREATE" )) }

   constructor (origin : KNode) : this()
   { this.origin = origin }

   override fun code () = Codes.CONNECT
   override fun originName () = "KConnectMessage"
}
