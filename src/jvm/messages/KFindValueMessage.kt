package kad.messages

import pen.Loggable

import pen.Config
import kad.dht.KGetParameter
import kad.node.KNode

/** Messages used to send to another node requesting content */
class KFindValueMessage () : Message, Loggable
{
   var origin                    = KNode()
   var params                    = KGetParameter()

   init
   { log( {"<FIND_VALUE>"}, Config.trigger( "KAD_MSG_CREATE" )) }

   constructor (origin : KNode, params : KGetParameter) : this()
   {
      this.origin = origin
      this.params = params
   }

   override fun code () = Codes.FIND_VALUE
   override fun originName () = "KFindValueMessage"
}
