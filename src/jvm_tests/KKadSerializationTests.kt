package kad.tests

import java.net.InetAddress
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import pen.Constants.SLASH
import pen.reeadObject; import pen.writeObject
import kad.KKademliaNode
import kad.dht.KContent
import kad.dht.KGetParameter
import kad.dht.KStorageEntry
import kad.dht.KDHT
import kad.node.KNode
import kad.node.KNodeId
import kad.routing.KRoutingTable
import kad.utils.KSerializableRoutingInfo

class KKadSerializationTests
{
   val OWNER = "Larsen"
   val outputFilename = "dist${SLASH}test.out"
   val KEY = ByteArray(20, { 0xFF.toByte() })

   @Test
   fun `Serializing KKademliaNode` ()
   {
      /* Creating KKademliaNode. */
      val kServiceNode = KKademliaNode()
      kServiceNode.ownerName = OWNER

      /* Writing KKademliaNode. */
      writeObject( kServiceNode, outputFilename )

      /* Reading KKademliaNode. */
      val deserialized = readObject<KKademliaNode>( outputFilename )

      /* Testing. */
      if (deserialized is KKademliaNode)
         Assertions.assertEquals( OWNER, deserialized.ownerName )
      else
         Assertions.assertTrue( false )
   }

   @Test
   fun `Serializing KNode` ()
   {
      /* Creating a KNode. */
      val kNodeID = KNodeId( KEY )
      val kNode = KNode( kNodeID, InetAddress.getLocalHost(), 49152 )

      /* Writing KNode. */
      writeObject( kNode, outputFilename )

      /* Reading KNode. */
      val deserialized = readObject<KNode>( outputFilename )

      /* Tests */
      if (deserialized is KNode)
      {
         Assertions.assertArrayEquals( KEY, deserialized.nodeId.keyBytes )
         Assertions.assertEquals( kNode.inetAddress.getHostAddress(), deserialized.inetAddress.getHostAddress() )
         Assertions.assertEquals( kNode.port, deserialized.port )
      }
      else
         Assertions.assertTrue( false )
   }

   @Test
   fun `Serializing KRoutingTable` ()
   {
      /* Creating KRoutingTable. */
      var kRoutingTable = KRoutingTable()
      val kNode = KNode()
      kNode.nodeId = KNodeId( KEY )
      kRoutingTable.initialize( kNode )

      /* Writing KRoutingTable. */
      writeObject(KSerializableRoutingInfo( kRoutingTable ), outputFilename)

      /* Reading KRoutingTable. */
      val deserialized = readObject<KSerializableRoutingInfo>( outputFilename )

      /* Testing. */
      if (deserialized is KSerializableRoutingInfo)
      {
         kRoutingTable = deserialized.toRoutingTable()
         Assertions.assertArrayEquals( KEY, kRoutingTable.node.nodeId.keyBytes )
      }
      else
         Assertions.assertTrue( false )
   }

   @Test
   fun `Serializing KDHT` ()
   {
      /* Creating KDHT. */
      val kDHT = KDHT()
      kDHT.initialize( OWNER )

      /* Writing KDHT. */
      writeObject( kDHT, outputFilename )

      /* Reading KDHT. */
      val deserialized = readObject<KDHT>( outputFilename )

      /* Testing. */
      if (deserialized is KDHT)
         Assertions.assertEquals( OWNER, deserialized.ownerName )
      else
         Assertions.assertTrue( false )
   }

   @Test
   fun `Serializing KStorageEntry` ()
   {
      val PAYLOAD = "Hello world!"

      /* Creating KContent. */
      val kStorageEntry = KStorageEntry(KContent( OWNER, PAYLOAD ))

      /* Writing KContent. */
      writeObject( kStorageEntry, outputFilename )

      /* Reading KContent. */
      val deserialized = readObject<KStorageEntry>( outputFilename )

      /* Testing. */
      if (deserialized is KStorageEntry)
      {
         Assertions.assertEquals( OWNER, deserialized.content.ownerName )
         Assertions.assertEquals( PAYLOAD, deserialized.content.value )
      }
      else
         Assertions.assertTrue( false )
   }
}
