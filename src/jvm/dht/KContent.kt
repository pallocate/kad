package kad.dht

import kotlinx.serialization.Serializable
import kad.node.KNodeId

@Serializable
class KContent ()
{
   var nodeId                                     = KNodeId()

   var ownerName                                  = ""
   var timestamp                                  = System.currentTimeMillis()/1000L
   var lastUpdated                                = timestamp
   var value                                      = ""
      set (value : String )
      {
         field = value
         updateTimestamp()
      }

   constructor (nodeId : KNodeId, ownerName : String) : this()
   {
      this.nodeId = nodeId
      this.ownerName = ownerName
   }

   constructor (ownerName : String, value : String) : this( KNodeId(), ownerName )
   { this.value = value }

   fun type () = "Content"
   fun updateTimestamp ()
   { lastUpdated = System.currentTimeMillis()/1000L }
}
