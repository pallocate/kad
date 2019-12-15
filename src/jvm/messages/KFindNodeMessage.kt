package kad.messages

import pen.Loggable

import pen.Config
import kad.node.KNode
import kad.node.KNodeId

/** A message sent to other nodes requesting the K-Closest nodes to a key sent in this message
     * @param origin The KNode from which the message is coming from
     * @param lookup The key for which to lookup nodes for */
class KFindNodeMessage () : Message, Loggable
{
   var origin                    = KNode()
   var lookupId                  = KNodeId()

   init
   { log( {"<FIND_NODE>"}, Config.trigger( "KAD_MSG_FIND_NODE" )) }

   constructor (origin : KNode, lookupId : KNodeId) : this()
   {
      this.origin = origin
      this.lookupId = lookupId
   }

   override fun code () = Codes.FIND_NODE
   override fun originName () = "KFindNodeMessage"
}
