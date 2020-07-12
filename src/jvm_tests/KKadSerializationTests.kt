package kad.tests

import java.net.InetAddress
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import pen.Constants.SLASH
import pen.serializeToFile
import pen.deserializeFromFile
import kad.KKademliaNode
import kad.dht.KContent
import kad.dht.KGetParameter
import kad.dht.KStorageEntry
import kad.dht.KDHT
import kad.node.KNode
import kad.node.KNodeId
import kad.routing.KRoutingTable

class KKadSerializationTests
{
   val OWNER = "Larsen"
   val outputFilename = "dist${SLASH}test.out"
   val ADDRESS = byteArrayOf( 1.toByte(), 1.toByte(), 1.toByte(), 1.toByte() )

   @Test
   fun `Serializing KNode` ()
   {
      /* Creating a KNode. */
      val kNode = KNode(KNodeId( ADDRESS ))

      /* Writing KNode. */
      serializeToFile( kNode, outputFilename, KNode.serializer() )

      /* Reading KNode. */
      val deserialized = deserializeFromFile<KNode>( outputFilename, KNode.serializer() )

      /* Tests */
      deserialized?.also {
         Assertions.assertEquals( ADDRESS, it.address )
      }
   }

   @Test
   fun `Serializing KRoutingTable` ()
   {
      /* Creating KRoutingTable. */
      var kRoutingTable = KRoutingTable()
      val kNode = KNode()
      val key = kNode.nodeId.key

      kRoutingTable.initialize( kNode )

      /* Writing KRoutingTable. */
      serializeToFile( kRoutingTable, outputFilename, KRoutingTable.serializer() )

      /* Reading KRoutingTable. */
      val deserialized = deserializeFromFile<KRoutingTable>( outputFilename, KRoutingTable.serializer() )

      /* Testing. */
      deserialized?.also {
         Assertions.assertArrayEquals( key, it.node.nodeId.key )
      }
   }

   @Test
   fun `Serializing KDHT` ()
   {
      /* Creating KDHT. */
      val kDHT = KDHT()

      /* Writing KDHT. */
      serializeToFile( kDHT, outputFilename, KDHT.serializer() )

      /* Reading KDHT. */
      val deserialized = deserializeFromFile<KDHT>( outputFilename, KDHT.serializer() )

      /* Testing. */
      deserialized?.also {
         Assertions.assertEquals( OWNER, it.ownerName )
      }
   }

   @Test
   fun `Serializing KStorageEntry` ()
   {
      val PAYLOAD = "Hello world!"

      /* Creating KContent. */
      val kStorageEntry = KStorageEntry(KContent( OWNER, PAYLOAD ))

      /* Writing KContent. */
      serializeToFile( kStorageEntry, outputFilename, KStorageEntry.serializer() )

      /* Reading KContent. */
      val deserialized = deserializeFromFile<KStorageEntry>( outputFilename, KStorageEntry.serializer() )

      /* Testing. */
      deserialized?.also {
         Assertions.assertEquals( OWNER, deserialized.content.ownerName )
         Assertions.assertEquals( PAYLOAD, deserialized.content.value )
      }
   }
}
