package kad.routing

import kad.node.KNode

/** Keeps information about contacts of the Node contacts are stored in the Buckets in the Routing Table.
  * Contacts are used instead of nodes because more information is needed than just the node information.
  * - Information such as
  * -- Last seen time */
class KContact (var node : KNode) : Comparable<KContact>
{
   /** The last time this contact was seen. */
   var lastSeen = 0L

   /** Stale as described by Kademlia paper page 64
     * When a contact fails to respond, if the replacement cache is empty and there is no replacement for the contact,
     * just mark it as stale.
     * Now when a new contact is added, if the contact is stale, it is removed. */
   var staleCount = 0

   /** Create a contact object
     * @param node The node associated with this contact */
   init
   {
      this.lastSeen = System.currentTimeMillis()/1000L
   }

   /** When a Node sees a contact a gain, the Node will want to update that it's seen recently,
   * this method updates the last seen timestamp for this contact. */
   fun setSeenNow () = System.currentTimeMillis()/1000L

   override fun equals (other : Any?) = if (other != null && other is KContact)
   other.node.equals( node ) else false

   /** Increments the amount of times this count has failed to respond to a request. */
   fun incrementStaleCount ()
   { staleCount++ }

   /** Reset the stale count of the contact if it's recently seen */
   fun resetStaleCount ()
   { staleCount = 0 }

   override fun compareTo (other : KContact) : Int
   {
      if (node.equals( other.node ))
         return 0

      return if (lastSeen > other.lastSeen) 1 else -1
   }

   override fun hashCode () = node.hashCode()
}
