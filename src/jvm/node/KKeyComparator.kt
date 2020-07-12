package kad.node

import java.math.BigInteger
import java.util.Comparator

/** A Comparator to compare 2 keys to a given key */
class KKeyComparator
/** @param key The KNodeId relative to which the distance should be measured. */
(nodeId : KNodeId) : Comparator<KNode>
{
   private val key : BigInteger

   init
   { key = nodeId.getInt() }

   /** Compare two objects which must both be of type `KNode`
     * and determine which is closest to the identifier specified in the constructor.
     * @param n1 KNode 1 to compare distance from the key
     * @param n2 KNode 2 to compare distance from the key */
   override fun compare (n1 : KNode, n2 : KNode) : Int
   {
      var b1 = n1.nodeId.getInt()
      var b2 = n2.nodeId.getInt()

      b1 = b1.xor( key )
      b2 = b2.xor( key )

      return b1.abs().compareTo( b2.abs() )
   }
}
