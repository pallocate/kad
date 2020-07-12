package kad.operations

import java.util.HashMap
import kad.node.KNode

/** Class that helps compute the route length taken to complete an operation. */
class KRouteLengthChecker
{
   /* Store the nodes and their route length (RL) */
   private val nodes : HashMap<KNode, Int>

   /* Lets cache the max route length instead of having to go and search for it later */
   /** Get the route length of the operation!
     * It will be the max route length of all the nodes here.
     * @return The route length */
   var routeLength : Int = 0
      private set

   init
   {
      this.nodes = HashMap()
      this.routeLength = 1
   }

   /** Add the initial nodes in the routing operation.
   * @param initialNodes The set of initial nodes */
   fun addInitialNodes (initialNodes : Collection<KNode>)
   {
      for (node in initialNodes)
      {nodes.put( node, 1 )}
   }

   /** Add any nodes that we get from a node reply.
     * The route length of these nodes will be their sender + 1;
     * @param inputSet The set of nodes we receive
     * @param sender   The node who send the set */
   fun addNodes (inputSet : Collection<KNode>, sender : KNode)
   {
      if (!nodes.containsKey( sender ))
         return

      /* Get the route length of the input set - sender RL + 1 */
      val inputSetRL = nodes.get( sender )!! + 1

      if (inputSetRL > routeLength)
         routeLength = inputSetRL

      /* Add the nodes to our set */
      for (node in inputSet)
         if (!nodes.containsKey( node ))                                        // We only add if the node is not already there... */
            nodes.put( node, inputSetRL )
   }
}
