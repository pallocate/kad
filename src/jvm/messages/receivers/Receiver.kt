package kad.messages.receivers

import pen.Log
import pen.PlaceHolder

import pen.Config
import kad.messages.Message

/** A receiver waits for incoming messages and perform some action when the message is received */
interface Receiver
{
   fun receive (message : Message, conversationId : Int)

   /** If no reply is received in `MessageServer.TIMEOUT`, KServer calls this method. */
   fun timeout (conversationId : Int) = Log.debug( "message timeout" )
}
class NoReceiver : Receiver, PlaceHolder
{
   override fun receive (message : Message, conversationId : Int) = unit( "NoReceiver received a message" )
}
