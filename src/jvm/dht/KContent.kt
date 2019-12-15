package kad.dht

import kad.node.KNodeId

class KContent ()
{
   var key                                        = KNodeId()

   var ownerName                                  = ""
   var timestamp                                  = System.currentTimeMillis()/1000L
   var lastUpdated                                = timestamp
   var value                                      = ""
      set (value : String )
      {
         field = value
         updateTimestamp()
      }

   constructor (key : KNodeId, ownerName : String) : this()
   {
      this.key = key
      this.ownerName = ownerName
   }

   constructor (ownerName : String, value : String) : this( KNodeId(), ownerName )
   { this.value = value }

   fun type () = "KContent"
   fun updateTimestamp ()
   { lastUpdated = System.currentTimeMillis()/1000L }
}
