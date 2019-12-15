package kad.tests

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.net.InetAddress
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import kad.dht.KGetParameter
import kad.dht.KContent
import kad.dht.KStorageEntry
import kad.node.KNode
import kad.node.KNodeId
import kad.messages.*
import kad.utils.KMessageSerializer

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
      KMessageSerializer.write( kSimpleMessage, outputStream )
      val serialized = outputStream.toByteArray()

      /* Unstreaming message. */
      val inputStream = ByteArrayInputStream( serialized )
      val deserialized = KMessageSerializer.read<KSimpleMessage>( inputStream )

      /* Testing. */
      if (deserialized is KSimpleMessage)
         Assertions.assertEquals( OWNER, deserialized.content )
      else
         Assertions.assertTrue( false )
   }

   @Test
   fun `Streaming KConnectMessage` ()
   {
      val kConnectMessage = KConnectMessage( KNode(KNodeId( KEY ), InetAddress.getLocalHost(), 49152) )

      /* Streaming message. */
      val outputStream = ByteArrayOutputStream()
      KMessageSerializer.write( kConnectMessage, outputStream )
      val serialized = outputStream.toByteArray()

      /* Unstreaming message. */
      val inputStream = ByteArrayInputStream( serialized )
      val deserialized = KMessageSerializer.read<KConnectMessage>( inputStream )

      /* Testing. */
      if (deserialized is KConnectMessage)
         Assertions.assertArrayEquals( KEY, deserialized.origin.nodeId.keyBytes )
      else
         Assertions.assertTrue( false )
   }

   @Test
   fun `streaming KAcknowledgeMessage` ()
   {
      val kAcknowledgeMessage = KAcknowledgeMessage( KNode(KNodeId( KEY ), InetAddress.getLocalHost(), 49152) )

      /* Streaming message. */
      val outputStream = ByteArrayOutputStream()
      KMessageSerializer.write( kAcknowledgeMessage, outputStream )
      val serialized = outputStream.toByteArray()

      /* Unstreaming message. */
      val inputStream = ByteArrayInputStream( serialized )
      val deserialized = KMessageSerializer.read<KAcknowledgeMessage>( inputStream )

      if (deserialized is KAcknowledgeMessage)
         Assertions.assertArrayEquals( KEY, deserialized.origin.nodeId.keyBytes )
      else
         Assertions.assertTrue( false )
   }

   @Test
   fun `streaming KFindNodeMessage` ()
   {
      val kFindNodeMessage = KFindNodeMessage(KNode(), KNodeId( KEY ))

      /* Streaming message. */
      val outputStream = ByteArrayOutputStream()
      KMessageSerializer.write( kFindNodeMessage, outputStream )
      val serialized = outputStream.toByteArray()

      /* Unstreaming message. */
      val inputStream = ByteArrayInputStream( serialized )
      val deserialized = KMessageSerializer.read<KFindNodeMessage>( inputStream )

      /* Testing. */
      if (deserialized is KFindNodeMessage)
         Assertions.assertArrayEquals( KEY, deserialized.lookupId.keyBytes )
      else
         Assertions.assertTrue( false )
   }

   @Test
   fun `Streaming KFindNodeReply` ()
   {
      val nodes = ArrayList<KNode>()
      nodes.add( KNode(KNodeId( KEY ), InetAddress.getLocalHost(), 49152) )
      val kFindNodeReply = KFindNodeReply( KNode(), nodes )

      /* Streaming message. */
      val outputStream = ByteArrayOutputStream()
      KMessageSerializer.write( kFindNodeReply, outputStream )
      val serialized = outputStream.toByteArray()

      /* Unstreaming message. */
      val inputStream = ByteArrayInputStream( serialized )
      val deserialized = KMessageSerializer.read<KFindNodeReply>( inputStream )

      /* Testing. */
      if (deserialized is KFindNodeReply)
         Assertions.assertArrayEquals( KEY, deserialized.nodes.first().nodeId.keyBytes )
      else
         Assertions.assertTrue( false )
   }

   @Test
   fun `Streaming KFindValueMessage` ()
   {
      val TYPE = "FIND_VALUE"
      val kGetParameter = KGetParameter(KNodeId(), TYPE, OWNER)
      val kFindValueMessage = KFindValueMessage( KNode(), kGetParameter )

      /* Streaming message. */
      val outputStream = ByteArrayOutputStream()
      KMessageSerializer.write( kFindValueMessage, outputStream )
      val serialized = outputStream.toByteArray()

      /* Unstreaming message. */
      val inputStream = ByteArrayInputStream( serialized )
      val deserialized = KMessageSerializer.read<KFindValueMessage>( inputStream )

      /* Testing. */
      if (deserialized is KFindValueMessage)
      {
         Assertions.assertEquals( OWNER, deserialized.params.ownerName )
         Assertions.assertEquals( TYPE, deserialized.params.type )
      }
      else
         Assertions.assertTrue( false )
   }

   @Test
   fun `Streaming KStoreMessage` ()
   {
      val kStorageEntry = KStorageEntry(KContent( KNodeId(), OWNER ))
      val kStoreMessage = KStoreMessage( KNode(), kStorageEntry )

      /* Streaming message. */
      val outputStream = ByteArrayOutputStream()
      KMessageSerializer.write( kStoreMessage, outputStream )
      val serialized = outputStream.toByteArray()

      /* Unstreaming message. */
      val inputStream = ByteArrayInputStream( serialized )
      val deserialized = KMessageSerializer.read<KStoreMessage>( inputStream )

      /* Testing. */
      if (deserialized is KStoreMessage)
         Assertions.assertEquals( OWNER, deserialized.payload.content.ownerName )
      else
         Assertions.assertTrue( false )
   }
}
