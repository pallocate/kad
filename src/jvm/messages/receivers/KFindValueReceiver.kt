package kad.messages.receivers

import java.util.NoSuchElementException
import pen.Loggable

import pen.Config
import kad.KServer
import kad.dht.KDHT
import kad.dht.KStorageEntry
import kad.messages.Message
import kad.messages.KFindValueMessage
import kad.messages.KFindNodeMessage
import kad.messages.KContentMessage
import kad.node.KNode
import kad.routing.KRoutingTable

/** Responds to a KFindValueMessage by sending a ContentMessage containing the requested content;
  * if the requested content is not found, a KFindNodeReply containing the K closest nodes to the request key is sent. */
class KFindValueReceiver (private val server : KServer, private val node : KNode, private val kRoutingTable : KRoutingTable, private val dht : KDHT) : Receiver, Loggable
{
   override fun receive (message : Message, conversationId : Int)
   {
      if (message is KFindValueMessage)
      {
         log({"message received"}, Config.trigger( "KAD_CONTENT_FIND" ))
         log({"content info: {owner: ${message.params.ownerName}}, {type: ${message.params.type}}, {key: \"${message.params.key}\"}"}, Config.trigger( "KAD_CONTENT_INFO" ))
         kRoutingTable.insert( message.origin )

         /* Check if we can have this data */
         if (dht.contains( message.params ))
         {
            try
            {
               /* Return a ContentMessage with the required data */
               val storageEntry = dht.get( message.params )
               if (storageEntry is KStorageEntry)
               {
                  val kContentMessage = KContentMessage( node, storageEntry )
                  server.reply( message.origin, kContentMessage, conversationId )
               }
            }
            catch (e : NoSuchElementException)
            {/* @todo Not sure why this exception is thrown here, checkup the system when tests are writtem*/}
         }
         else
         {
            /* Return a the K closest nodes to this content identifier
             * We create a KFindNodeReceiver and let this receiver handle this operation */
            val lkpMsg = KFindNodeMessage( message.origin, message.params.key )
            KFindNodeReceiver( server, node, kRoutingTable ).receive( lkpMsg, conversationId )
         }
      }
   }

   override fun originName () = "KFindValueReceiver(${node})"
}
