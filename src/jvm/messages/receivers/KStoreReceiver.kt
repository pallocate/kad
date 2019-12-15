package kad.messages.receivers

import java.io.IOException
import kad.KServer
import kad.messages.Message
import kad.dht.KDHT
import kad.dht.KStorageEntry
import kad.messages.KStoreMessage
import kad.routing.KRoutingTable

/** Receiver for incoming KStoreMessage */
class KStoreReceiver (private val server : KServer, private val routingTable : KRoutingTable, private val dht : KDHT) : Receiver
{
   override fun receive (message : Message, conversationId : Int)
   {
      if (message is KStoreMessage)
      {
         /* Insert the message sender into this node's routing table */
         routingTable.insert( message.origin )

         try
         {   /* KStoreMessage this KContent into the DHT */
            val content = message.payload
            if (content is KStorageEntry)
               dht.store( content )
         }
         catch (e: IOException)
         {println( "Unable to store received content; Message: " + e.message )}
      }
   }
}
