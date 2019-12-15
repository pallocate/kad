package kad.messages.receivers

import kad.messages.Message

/** Default receiver if none other is called */
class KSimpleReceiver : Receiver
{
   override fun receive (message : Message, conversationId : Int) {}
}
