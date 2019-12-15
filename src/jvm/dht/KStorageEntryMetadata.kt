package kad.dht

import java.util.Objects
import kad.StorageEntryMetadata
import kad.node.KNodeId

/** Keeps track of data for a KContent stored in the DHT, Used by the ContentManager */
class KStorageEntryMetadata () : StorageEntryMetadata
{
   var key                                        = KNodeId()
   var ownerName                                  = ""
   var type                                       = ""
   var contentHash                                = 0
   var lastUpdated                                = 0L

   /* This value is the last time this content was last updated from the network */
   var lastRepublished                            = 0L

   constructor (content : KContent) : this()
   {
      key = content.key
      ownerName = content.ownerName
      type = content.type()
      contentHash = hashCode()
      this.lastUpdated = content.lastUpdated
      lastRepublished = System.currentTimeMillis()/1000L
   }

   /** Whenever we republish a content or get this content from the network, we update the last republished time */
   fun updateLastRepublished ()
   { lastRepublished = System.currentTimeMillis()/1000L }

   /** When a node is looking for content, he sends the search criteria in a KGetParameter object
     * Here we take this KGetParameter object and check if this StorageEntry satisfies the given parameters
     * @return boolean Whether this content satisfies the parameters */
   fun satisfiesParameters (params : KGetParameter) : Boolean
   {
      /* Check that owner id matches */
      if ((!params.ownerName.equals( ownerName )))
         return false

      /* Check that type matches */
      if ((!params.type.equals( type )))
         return false

      /* Check that key matches */
      if ((!params.key.equals( key )))
         return false

      return true
   }

   override fun equals (other : Any?) = if (other != null && other is KStorageEntryMetadata)
   (hashCode() == other.hashCode()) else false

   override fun hashCode () : Int
   {
      var hash = 3
      hash = 23 * hash + Objects.hashCode( this.key )
      hash = 23 * hash + Objects.hashCode( this.ownerName )
      hash = 23 * hash + Objects.hashCode( this.type )

      return hash
   }

   override fun toString () ="[ StorageEntry : {Key : $key}{Owner : $ownerName}{Type : $type} ]"
}
