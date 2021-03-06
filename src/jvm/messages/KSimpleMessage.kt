package kad.messages

import kotlinx.serialization.Serializable
import pen.Loggable
import pen.Config

/** A simple message used for testing the system; Default message constructed if the message type sent is not available */
@Serializable
class KSimpleMessage () : Message(), Loggable
{
   var content = ""

   init
   { log(Config.trigger( "KAD_MSG_CREATE" )) }

   constructor (content : String) : this()
   {
      this.content = content
   }

   override fun tag () = "KSimpleMessage"
}
