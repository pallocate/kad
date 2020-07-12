package kad.tests

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import kad.dht.KGetParameter
import kad.dht.KContent
import kad.dht.KStorageEntry
import kad.node.KNode
import kad.node.KNodeId
import kad.messages.*
import kad.MessageSerializer

class KMessagesTests
{
   val OWNER = "Emanuel"
   val KEY = ByteArray(20, { 0xAF.toByte() })

   @Test
   fun `Streaming KSimpleMessage` ()
   {
      val kSimpleMessage = KSimpleMessage( OWNER )

      /* Streaming message. */
      val outputStream = ByteArrayOutputStream()
      MessageSerializer.writeMessage( kSimpleMessage, outputStream )
      val serialized = outputStream.toByteArray()

      /* Unstreaming message. */
      val inputStream = ByteArrayInputStream( serialized )
      val deserialized = MessageSerializer.readMessage( inputStream )

      /* Testing. */
      if (deserialized is KSimpleMessage)
         Assertions.assertEquals( OWNER, deserialized.content )
      else
         Assertions.assertTrue( false )
   }

   @Test
   fun `Streaming KConnectMessage` ()
   {
      val kNode = KNode()
      val key = kNode.nodeId.key
      val kConnectMessage = KConnectMessage( kNode )

      /* Streaming message. */
      val outputStream = ByteArrayOutputStream()
      MessageSerializer.writeMessage( kConnectMessage, outputStream )
      val serialized = outputStream.toByteArray()

      /* Unstreaming message. */
      val inputStream = ByteArrayInputStream( serialized )
      val deserialized = MessageSerializer.readMessage( inputStream )

      /* Testing. */
      Assertions.assertArrayEquals( key, (deserialized as KConnectMessage).origin.nodeId.key )
   }

   @Test
   fun `streaming KAcknowledgeMessage` ()
   {
      val kNode = KNode()
      val key = kNode.nodeId.key
      val kAcknowledgeMessage = KAcknowledgeMessage( kNode )

      /* Streaming message. */
      val outputStream = ByteArrayOutputStream()
      MessageSerializer.writeMessage( kAcknowledgeMessage, outputStream )
      val serialized = outputStream.toByteArray()

      /* Unstreaming message. */
      val inputStream = ByteArrayInputStream( serialized )
      val deserialized = MessageSerializer.readMessage( inputStream )

      Assertions.assertArrayEquals( key, (deserialized as KAcknowledgeMessage).origin.nodeId.key )
   }

   @Test
   fun `streaming KFindNodeMessage` ()
   {
      val kFindNodeMessage = KFindNodeMessage(KNode(), KNodeId( KEY ))

      /* Streaming message. */
      val outputStream = ByteArrayOutputStream()
      MessageSerializer.writeMessage( kFindNodeMessage, outputStream )
      val serialized = outputStream.toByteArray()

      /* Unstreaming message. */
      val inputStream = ByteArrayInputStream( serialized )
      val deserialized = MessageSerializer.readMessage( inputStream )

      /* Testing. */
      Assertions.assertArrayEquals( KEY, (deserialized as KFindNodeMessage).lookupId.key )
   }

   @Test
   fun `Streaming KFindNodeReply` ()
   {
      val nodes = ArrayList<KNode>()
      val kNode = KNode()
      val key = kNode.nodeId.key

      nodes.add( kNode )
      val kFindNodeReply = KFindNodeReply( KNode(), nodes )

      /* Streaming message. */
      val outputStream = ByteArrayOutputStream()
      MessageSerializer.writeMessage( kFindNodeReply, outputStream )
      val serialized = outputStream.toByteArray()

      /* Unstreaming message. */
      val inputStream = ByteArrayInputStream( serialized )
      val deserialized = MessageSerializer.readMessage( inputStream )

      /* Testing. */
      Assertions.assertArrayEquals( key, (deserialized as KFindNodeReply).nodes.first().nodeId.key )
   }

   @Test
   fun `Streaming KFindValueMessage` ()
   {
      val TYPE = "FIND_VALUE"
      val kGetParameter = KGetParameter(KNodeId(), TYPE, OWNER)
      val kFindValueMessage = KFindValueMessage( KNode(), kGetParameter )

      /* Streaming message. */
      val outputStream = ByteArrayOutputStream()
      MessageSerializer.writeMessage( kFindValueMessage, outputStream )
      val serialized = outputStream.toByteArray()

      /* Unstreaming message. */
      val inputStream = ByteArrayInputStream( serialized )
      val deserialized = MessageSerializer.readMessage( inputStream )

      /* Testing. */
      Assertions.assertEquals( OWNER, (deserialized as KFindValueMessage).params.ownerName )
      Assertions.assertEquals( TYPE, (deserialized as KFindValueMessage).params.type )
   }

   @Test
   fun `Streaming KStoreMessage` ()
   {
      val kStorageEntry = KStorageEntry(KContent( KNodeId(), OWNER ))
      val kStoreMessage = KStoreMessage( KNode(), kStorageEntry )

      /* Streaming message. */
      val outputStream = ByteArrayOutputStream()
      MessageSerializer.writeMessage( kStoreMessage, outputStream )
      val serialized = outputStream.toByteArray()

      /* Unstreaming message. */
      val inputStream = ByteArrayInputStream( serialized )
      val deserialized = MessageSerializer.readMessage( inputStream )

      /* Testing. */
      Assertions.assertEquals( OWNER, (deserialized as KStoreMessage).payload.content.ownerName )
   }
}
