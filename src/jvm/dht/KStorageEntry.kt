package kad.dht

import kotlinx.serialization.Serializable
import kad.StorageEntry

/** A StorageEntry class that is used to store a content on the DHT */
@Serializer
class KStorageEntry () : StorageEntry, Filable
{
   var content                                    = KContent()
   var contentMetadata                            = KStorageEntryMetadata()

   constructor (content : KContent, contentMetadata : KStorageEntryMetadata = KStorageEntryMetadata( content )) : this()
   {
      this.content = content
      this.contentMetadata = contentMetadata
   }
}
