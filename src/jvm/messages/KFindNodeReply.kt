package kad.messages

import pen.Loggable

import pen.Config
import kad.node.KNode

/** A message used to connect nodes.
  * When a NodeLookup Request comes in, we respond with a KFindNodeReply */
class KFindNodeReply () : Message, Loggable
{
   var origin                    = KNode()
   var nodes                     = ArrayList<KNode>()

   init
   { log( {"<FIND_NODE_REPLY>"}, Config.trigger( "KAD_MSG_FIND_NODE" )) }

   constructor (origin : KNode, nodes : ArrayList<KNode>) : this()
   {
      this.origin = origin
      this.nodes = nodes
   }

   override fun code () = Codes.FIND_NODE_REPLY
   override fun originName () = "KFindNodeReply"
}
