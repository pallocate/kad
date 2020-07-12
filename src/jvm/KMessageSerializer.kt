package kad

import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.modules.SerializersModule
import pen.Loggable
import pen.LogLevel.WARN
import pen.LogLevel.ERROR
import pen.deserializeFromString
import pen.serializeToString
import pen.Config
import kad.messages.*

object MessageSerializer : Loggable
{
   private val polymorphicModule = SerializersModule {
       polymorphic( Message::class ) {
           KAcknowledgeMessage::class with KAcknowledgeMessage.serializer()
           KConnectMessage::class with KConnectMessage.serializer()
           KContentMessage::class with KContentMessage.serializer()
           KFindValueMessage::class with KFindValueMessage.serializer()
           KFindNodeMessage::class with KFindNodeMessage.serializer()
           KFindNodeReply::class with KFindNodeReply.serializer()
           KStoreMessage::class with KStoreMessage.serializer()
           KSimpleMessage::class with KSimpleMessage.serializer()
       }
   }

   fun readMessage (inputStream : InputStream) : Message
   {
      log({"reading message"}, Config.trigger( "KAD_STREAMING" ))
      var ret : Message = NoMessage()

      try
      {
         val jsonString = inputStream.bufferedReader().use { it.readText() }
         val deserialized = deserializeFromString<Message>( jsonString, Message.serializer(), polymorphicModule )

         if (deserialized == null)
            log({ "reading message failed! (invalid json)" }, Config.trigger( "KAD_MSG_CREATE" ), WARN)
         else
            ret = deserialized
      }
      catch (e : Exception)
      { log("reading message failed! (${e.message})", Config.trigger( "KAD_STREAMING" ), WARN) }

      return ret
   }

   fun writeMessage (message : Message, outputStream : OutputStream)
   {
      log({"writing message"}, Config.trigger( "KAD_STREAMING" ))

      try
      {
         val jsonString = serializeToString<Message>( message, Message.serializer(), polymorphicModule )
         outputStream.write( jsonString.toByteArray() )
         outputStream.flush()
      }
      catch (e : Exception)
      {log( "Writing message failed! (${e.message})", Config.trigger( "KAD_STREAMING" ), ERROR )}
   }

   override fun tag () = "MessageSerializer"
}
