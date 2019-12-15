package kad.messages

import pen.Loggable

import pen.Config
import kad.node.KNode

/** A message used to acknowledge a request from a node; can be used in many situations.
  * - Mainly used to acknowledge a connect message */
class KAcknowledgeMessage () : Message, Loggable
{
   var origin = KNode()

   init
   { log( {"<ACKNOWLEDGE>"}, Config.trigger( "KAD_MSG_CREATE" )) }

   constructor (origin : KNode) : this()
   { this.origin = origin }

   override fun code () = Codes.ACKNOWLEDGE
   override fun originName () = "KAcknowledgeMessage"
}
