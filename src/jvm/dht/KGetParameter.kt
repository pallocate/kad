package kad.dht

import kotlinx.serialization.Serializable
import kad.node.KNodeId

@Serializable
class KGetParameter ()
{
   var nodeId                                          = KNodeId()
   var ownerName                                       = ""
   var type                                            = ""

   /** Construct a KGetParameter to search for data by KNodeId and owner */
   constructor (nodeId : KNodeId, type : String) : this()
   {
      this.nodeId = nodeId
      this.type = type
   }

   /** Construct a KGetParameter to search for data by KNodeId, owner, type */
   constructor (nodeId: KNodeId, type: String, owner: String) : this( nodeId, type )
   { this.ownerName = owner }

   /** Construct our get parameter from a KContent */
   constructor (content : KContent) : this()
   {
      this.nodeId = content.nodeId
      this.type = content.type()
      this.ownerName = content.ownerName
   }

   /** Construct our get parameter from a StorageEntryMeta data */
   constructor (metaData: KStorageEntryMetadata) : this()
   {
      this.nodeId = metaData.nodeId
      this.type = metaData.type
      this.ownerName = metaData.ownerName
   }
}
