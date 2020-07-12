package kad.tests

import java.net.InetAddress
import org.junit.jupiter.api.*

import kademlia.DefaultConfiguration
import kademlia.routing.*
import kademlia.node.Node
import kademlia.node.KademliaId
import kad.routing.*
import kad.node.KNode
import kad.node.KNodeId

class KRoutingTableTests
{
   val jNode = Node(KademliaId( "ASF456789djem4567463" ), InetAddress.getLocalHost(), 12050)
   val jOtherNode = Node(KademliaId( "AS84k678DJRW84567465" ), InetAddress.getLocalHost(), 4586)

   val kNode = KNode(KNodeId( "ASF456789djem4567463" ), 12051)
   val kOtherNode = KNode(KNodeId( "AS84k678DJRW84567465" ), 4587)

   val jKademliaRoutingTable : JKademliaRoutingTable = JKademliaRoutingTable( jNode, DefaultConfiguration() )
   val kRoutingTable : KRoutingTable = KRoutingTable()

   init
   {
      kRoutingTable.initialize( kNode )
      jKademliaRoutingTable.insert(Contact( jOtherNode ))
      kRoutingTable.insert( KContact( kOtherNode ) )
   }

   @Test
   fun `Initial size` ()
   {
      Assertions.assertEquals( kRoutingTable.allContacts().size, jKademliaRoutingTable.getAllContacts().size )
      Assertions.assertEquals( kRoutingTable.allNodes().size, jKademliaRoutingTable.getAllNodes().size )
   }

   @Test
   fun `Contains node` ()
   {
      val jBuckets = jKademliaRoutingTable.getBuckets()
      val kBuckets = kRoutingTable.buckets

      Assertions.assertTrue(jBuckets[142].containsNode( jOtherNode ))
      Assertions.assertTrue(kBuckets[142].containsContact(KContact( kOtherNode )) )
   }
}
