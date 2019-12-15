package kad.messages

/** The unique code for the message type, used to differentiate all messages
  * from each other. Since this is of `byte` type there can
  * be at most 256 different message types. */
object Codes
{
   const val ACKNOWLEDGE         = 0x01.toByte()
   const val CONNECT             = 0x02.toByte()                                // Connect
   const val FIND_VALUE          = 0x03.toByte()                                // Same as FIND_NODE
   const val CONTENT             = 0x04.toByte()                                // Send content
   const val FIND_NODE           = 0x05.toByte()                                // The recipient of the request will return the k nodes in his own buckets that are the closest ones to the requested key.
   const val FIND_NODE_REPLY     = 0x06.toByte()                                // Node reply
   const val SIMPLE              = 0x07.toByte()                                // Simple message
   const val STORE               = 0x08.toByte()                                // Stores a (key, value) pair in one node.

   const val PING                = 0x09.toByte()                                // used to verify that a node is still alive.
/*
   fun messageName (code : Byte) = when (code)
   {
      ACKNOWLEDGE                -> "<ACKNOWLEDGE>"
      CONNECT                    -> "<CONNECT>"
      CONTENT                    -> "<CONTENT>"
      FIND_VALUE                 -> "<FIND_VALUE>"
      FIND_NODE                  -> "<FIND_NODE>"
      FIND_NODE_REPLY            -> "<FIND_NODE_REPLY>"
      STORE                      -> "<STORE>"
      SIMPLE                     -> "<SIMPLE>"
      else                       -> "<INVALID>"
   }
*/
}
