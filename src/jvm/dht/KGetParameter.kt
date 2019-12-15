package kad.dht

import kad.node.KNodeId

class KGetParameter ()
{
   var key                                        = KNodeId()
   var ownerName                                  = ""
   var type                                       = ""

   /** Construct a KGetParameter to search for data by KNodeId and owner */
   constructor (key : KNodeId, type : String) : this()
   {
      this.key = key
      this.type = type
   }

   /** Construct a KGetParameter to search for data by KNodeId, owner, type */
   constructor (key: KNodeId, type: String, owner: String) : this( key, type )
   { this.ownerName = owner }

   /** Construct our get parameter from a KContent */
   constructor (content : KContent) : this()
   {
      this.key = content.key
      this.type = content.type()
      this.ownerName = content.ownerName
   }

   /** Construct our get parameter from a StorageEntryMeta data */
   constructor (metaData: KStorageEntryMetadata) : this()
   {
      this.key = metaData.key
      this.type = metaData.type
      this.ownerName = metaData.ownerName
   }
}
