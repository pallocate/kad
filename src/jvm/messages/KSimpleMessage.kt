package kad.messages

import pen.Loggable

import pen.Config

/** A simple message used for testing the system; Default message constructed if the message type sent is not available */
class KSimpleMessage () : Message, Loggable
{
   var content = ""

   init
   { log( {"<SIMPLE>"}, Config.trigger( "KAD_MSG_CREATE" )) }

   constructor (content : String) : this()
   {
      this.content = content
   }

   override fun code () = Codes.SIMPLE
   override fun originName () = "KSimpleMessage"
}
