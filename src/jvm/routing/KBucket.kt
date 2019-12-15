package kad.routing

import java.util.ArrayList
import java.util.NoSuchElementException
import java.util.TreeSet
import kad.Constants
import kad.node.KNode

/** A bucket in the Kademlia routing table */
class KBucket ()
{
   /** Contacts stored in this routing table */
   val contacts                                   = TreeSet<KContact>()

   /** A set of last seen contacts that can replace any current contact that is unresponsive */
   private val replacementCache                   = TreeSet<KContact>()
   /** How deep is this bucket in the Routing Table */
   private var depth                              = 0

   constructor (depth : Int) : this()
   {
      this.depth = depth
   }

   @Synchronized
   fun insert (contact : KContact)
   {
      if (contacts.contains( contact ))
      {
         /* If the contact is already in the bucket, lets update that we've seen it
          * We need to remove and re-add the contact to get the Sorted Set to update sort order */
         val tmp = removeFromContacts( contact.node )
         tmp.setSeenNow()
         tmp.resetStaleCount()
         contacts.add( tmp )
      }
      else
      {
         /* If the bucket is filled, so put the contacts in the replacement cache */
         if (contacts.size >= Constants.K)
         {
            /* If the cache is empty, we check if any contacts are stale and replace the stalest one */
            var stalest : KContact? = null
            for (tmp in contacts)
            {
               if (tmp.staleCount >= Constants.STALE)
               {
                  /* KContact is stale */
                  if (stalest == null)
                     stalest = tmp
                  else
                     if (tmp.staleCount > stalest.staleCount)
                         stalest = tmp
               }
            }

            /* If we have a stale contact, remove it and add the new contact to the bucket */
            if (stalest == null)
            {
               /* No stale contact, lets insert this into replacement cache */
               insertIntoReplacementCache( contact )
            }
            else
            {
               contacts.remove( stalest )
               contacts.add( contact )
            }
         }
         else
            contacts.add( contact )
      }
   }


   @Synchronized
   fun insert (node : KNode)
   {
      insert(KContact( node ))
   }

   @Synchronized
   fun containsContact (contact : KContact) : Boolean
   {
      return contacts.contains( contact )
   }

   @Synchronized
   fun containsNode (node : KNode) = containsContact(KContact( node ))

   @Synchronized
   fun removeContact (contact : KContact) : Boolean
   {
      /* If the contact does not exist, then we failed to remove it */
      if (!contacts.contains( contact ))
         return false

      /* Contact exist, lets remove it only if our replacement cache has a replacement */
      if (!replacementCache.isEmpty())
      {
         /* Replace the contact with one from the replacement cache */
         this.contacts.remove( contact )
         val replacement = replacementCache.first()
         contacts.add( replacement )
         replacementCache.remove( replacement )
      }
      else                                                                      // There is no replacement, just increment the contact's stale count
         getFromContacts( contact.node ).incrementStaleCount()

      return true
   }

   @Synchronized
   private fun getFromContacts (node : KNode) : KContact
   {
      for (contact in this.contacts)
         if (contact.node.equals( node ))
            return contact

      /* This contact does not exist */
      throw NoSuchElementException( "The contact does not exist in the contacts list." )
   }

   @Synchronized
   private fun removeFromContacts (node : KNode) : KContact
   {
      for (c in contacts)
         if (c.node.equals( node ))
         {
            contacts.remove( c )
            return c
         }

      /* We got here means this element does not exist */
      throw NoSuchElementException( "Node does not exist in the replacement cache." )
   }

   @Synchronized
   fun removeNode (node: KNode) = removeContact(KContact( node ))

   @Synchronized
   fun numContacts () = contacts.size

   /** When the bucket is filled, we keep extra contacts in the replacement cache. */
   @Synchronized
   private fun insertIntoReplacementCache (contact : KContact)
   {
      /* Just return if this contact is already in our replacement cache */
      if (replacementCache.contains( contact ))
      {
         /** If the contact is already in the bucket, lets update that we've seen it
           * We need to remove and re-add the contact to get the Sorted Set to update sort order */
         val tmp = removeFromReplacementCache( contact.node )
         tmp.setSeenNow()
         replacementCache.add( tmp )
      }
      else
         if (replacementCache.size > Constants.K)
         {
            /* if our cache is filled, we remove the least recently seen contact */
            replacementCache.remove( replacementCache.last() )
            replacementCache.add( contact )
         }
         else
            replacementCache.add(contact)
   }

   @Synchronized
   private fun removeFromReplacementCache (node : KNode) : KContact
   {
      for (c in replacementCache)
         if (c.node.equals( node ))
         {
            replacementCache.remove( c )
            return c
         }

      /* We got here means this element does not exist */
      throw NoSuchElementException( "Node does not exist in the replacement cache." )
   }


   @Synchronized
   override fun toString () : String
   {
      val sb = StringBuilder( "Bucket at depth: $depth\n" )

      sb.append("\n  NODES\n")
      for (n in contacts)
      {
         sb.append( "KNode: ${n.node.nodeId.toString()}" )
         sb.append( " (stale: ${n.staleCount})\n" )
      }
      return sb.toString()
   }
   fun getDepth () = depth
}
