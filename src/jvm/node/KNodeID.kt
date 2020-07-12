package kad.node

import java.math.BigInteger
import java.util.Arrays
import java.util.BitSet
import java.util.Random
import kotlinx.serialization.Transient
import kotlinx.serialization.Serializable
import pen.Utils
import pen.toHex
import pen.ByteArraySerialiser

@Serializable
class KNodeId (@Serializable( with = ByteArraySerialiser::class )
               val key : ByteArray)
{
   companion object
   {
      const val ID_SIZE = 160
      const val KEY_SIZE = ID_SIZE/8

      fun randomKey () : ByteArray
      {
         val ret = ByteArray( KEY_SIZE )
         Random().nextBytes( ret )
         return ret
      }
   }

   private val sName by lazy {Utils.shortName( key )}

   constructor () : this( randomKey() ) {}

   /** Construct the KNodeId from a string. */
   constructor (string : String) : this( string.toByteArray() ) {}

   /** @param other The KNodeId to compare to this KNodeId. */
   override fun equals (other : Any?) = if (other is KNodeId)
   (hashCode() == other.hashCode()) else false

   override fun hashCode () : Int
   {
      var hash = 7
      hash = 83*hash + Arrays.hashCode( this.key )
      return hash
   }

   /** Checks the distance between this and another KNodeId
     * @return The distance of this KNodeId from the given KNodeId */
   fun xor (kNodeId : KNodeId) : KNodeId
   {
      val result = ByteArray( KEY_SIZE )
      val nidBytes = kNodeId.key

      try
      {
         for (i in 0 until KEY_SIZE)
            result[i] = (key[i].toInt() xor nidBytes[i].toInt()).toByte()
      }
      catch (e : Exception)
      { e.printStackTrace() }

      val resNid = KNodeId( result )

      return resNid
   }

   /** Generates a KNodeId that is some distance away from this KNodeId
     * @param distance in number of bits
     * @return KNodeId The newly generated KNodeId */
   fun generateNodeIdByDistance (distance : Int) : KNodeId
   {
      val result = ByteArray( KEY_SIZE )

      /* Since distance = ID_SIZE - prefixLength, we need to fill that amount with 0's */
      val numByteZeroes = (ID_SIZE - distance)/8
      val numBitZeroes = 8 - (distance % 8)

      /* Filling byte zeroes */
      for (i in 0 until numByteZeroes)
         result[i] = 0

      /* Filling bit zeroes */
      val bits = BitSet( 8 )
      bits.set( 0, 8 )

      for (i in 0 until numBitZeroes)
      {
         /* Shift 1 zero into the start of the value */
         bits.clear( i )
      }
      bits.flip( 0, 8 )                                                         // Flip the bits since they're in reverse order
      result[numByteZeroes] = bits.toByteArray()[0]

      /* Set the remaining bytes to Maximum value */
      for (i in numByteZeroes + 1 until result.size)
         result[i] = Byte.MAX_VALUE

      return xor(KNodeId( result ))
   }

   /** Counts the number of leading 0's in this KNodeId. */
   fun getFirstSetBitIndex () : Int
   {
      var prefixLength = 0

      for (b in key)
      {
         val byte = b.toInt()
         if (byte == 0)
            prefixLength += 8
         else
         {
            /* If the byte is not 0, we need to count how many MSBs are 0 */
            var count = 0
            for (i in 7 downTo 0)
            {
               val a = (byte and (1 shl i)) == 0
               if (a)
                  count++
               else
                  break                                                         // Reset the count if we encounter a non-zero number
            }

            /* Add the count of MSB 0s to the prefix length */
            prefixLength += count

            /* Break here since we've now covered the MSB 0s */
            break
         }
      }
      return prefixLength
   }

   /** Gets the distance from this KNodeId to another KNodeId
     * @return Integer The distance */
   fun getDistance (to : KNodeId) : Int
   {
      /** Compute the xor of this and to
        * Get the index i of the first set bit of the xor returned KNodeId
        * The distance between them is ID_SIZE - i */
      return ID_SIZE - this.xor( to ).getFirstSetBitIndex()
   }

   fun getInt () = BigInteger(1, key)

   fun shortName() = sName
   override fun toString () = key.toHex()
}
