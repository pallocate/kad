package kad.routing

import java.util.Comparator

/** A Comparator to compare 2 contacts by their last seen time */
class KContactLastSeenComparator : Comparator<KContact>
{
   /** Compare two contacts to determine their order in the Bucket,
     * KContacts are ordered by their last seen timestamp.
     * @param c1 KContact 1
     * @param c2 KContact 2 */
   override fun compare (c1 : KContact, c2 : KContact) : Int
   {
      return if (c1.node.equals( c2.node ))
         0
      else
      {
         /* We may have 2 different contacts with same last seen values so we can't return 0 here */
         if (c1.lastSeen > c2.lastSeen) 1 else -1
      }
   }
}
