package kad.messages

import pen.Log

interface Message
{
   fun code () : Byte
}
class NoMessage : Message
{
   override fun code () = 0x00.toByte()
}
