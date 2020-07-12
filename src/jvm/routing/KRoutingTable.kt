package kad.routing

import kotlinx.serialization.Serializable
import pen.Loggable
import pen.Config
import kad.node.KKeyComparator
import kad.node.KNode
import kad.node.KNodeId

/** A Kademlia routing table. */
@Serializable
class KRoutingTable () : Loggable
{
   /** This node in the Kademlia network. */
   var node = KNode()
   /** The buckets of this routing table. */
   var buckets = createBuckets()

   init
   { log( "created", Config.trigger( "KAD_CREATE" ), pen.LogLevel.INFO) }

   /** Initializes the routing table using this node in the Kademlia network. */
   fun initialize (kNode : KNode)
   {
      node = kNode
      log("initializing", Config.trigger( "KAD_INITIALIZE" ))
      insert( node )                                                            // Insert the local node
   }

   private fun createBuckets () : Array<KBucket>
   {
      var i = 0
      return Array<KBucket>( KNodeId.ID_SIZE, {KBucket( i++ )} )
   }

   /** A List of all Nodes in this KRoutingTable */
   @Synchronized
   fun allNodes () : ArrayList<KNode>
   {
      val nodes = ArrayList<KNode>()

      for (bucket in buckets)
         for (c in bucket.contacts)
            nodes.add( c.node )

      return nodes
   }

   /** A List of all Contacts in this KRoutingTable */
   fun allContacts () : ArrayList<KContact>
   {
       val contacts = ArrayList<KContact>()

       for (bucket in buckets)
           contacts.addAll( bucket.contacts )

       return contacts
   }

   /** Adds a contact to the routing table based on how far it is from the LocalNode. */
   @Synchronized
   fun insert (kContact : KContact)
   {
      log("inserting contact (${kContact.node})", Config.trigger( "KAD_CONTACT_PUT" ))
      log("contact info: {address: ${kContact.node.address}}", Config.trigger( "KAD_CONTACT_INFO" ))
      buckets[getBucketId( kContact.node.nodeId )].insert(kContact)
   }

   /** Adds a node to the routing table based on how far it is from the LocalNode. */
   @Synchronized
   fun insert (kNode : KNode)
   {
      log("inserting node (${kNode})", Config.trigger( "KAD_CONTACT_PUT" ))
      log("node info: {address: ${kNode.address}}}", Config.trigger( "KAD_CONTACT_INFO" ))
      buckets[getBucketId( kNode.nodeId )].insert(kNode)
   }

   /** Find the closest set of contacts to a given NodeID
     * @param target The NodeID to find contacts close to
     * @param numNodesRequired The number of contacts to find
     * @return List A List of contacts closest to target */
   @Synchronized
   fun findClosest (target : KNodeId, numNodesRequired : Int) : ArrayList<KNode>
   {
      val sortedSet = java.util.TreeSet(KKeyComparator( target ))
      sortedSet.addAll( allNodes() )                                            // toSortedSet() might possible be used in conjunction with KNode : Comparable<KNode>

      val closest = ArrayList<KNode>( numNodesRequired )

      /* Now we have the sorted set, lets get the top numRequired */
      var count = 0
      for (n in sortedSet)
      {
         closest.add( n )
         if (++count == numNodesRequired)
            break
      }
      return closest
   }

   /** Method used by operations to notify the routing table of any contacts that have been unresponsive.
     * @param contacts The set of unresponsive contacts */
   fun setUnresponsiveContacts (contacts : ArrayList<KNode>)
   {
      if (!contacts.isEmpty())
         for (contact in contacts)
            setUnresponsiveContact( contact )
   }

   /** Method used by operations to notify the routing table of any contacts that have been unresponsive. */
   @Synchronized
   fun setUnresponsiveContact (kNode : KNode)
   {
      val bucketId = this.getBucketId( kNode.nodeId )
      buckets[bucketId].removeNode( kNode )                                     // Remove the contact
   }

   override fun tag () = "KRoutingTable(${node})"

   @Synchronized
   override fun toString () : String
   {
      val sb = StringBuilder()
      var totalContacts = 0

      for (bucket in buckets)
         if (bucket.numContacts() > 0)
         {
            totalContacts += bucket.numContacts()
            sb.append( bucket.toString() + "\n" )
         }

      sb.append( "Total contacts: ${totalContacts}" )

      return sb.toString()
   }

   /** Compute the bucket ID in which a given node should be placed; the bucketId is computed based on how far the node is away from the Local Node.
     * @param kNodeId The NodeID for which we want to find which bucket it belong to
     * @return Integer The bucket ID in which the given node should be placed. */
   private fun getBucketId (kNodeId : KNodeId) : Int
   {
      val bId = node.nodeId.getDistance( kNodeId ) - 1

      /* If we are trying to insert a node into it's own routing table, then the bucket ID will be -1, so let's just keep it in bucket 0 */
      return if (bId < 0) 0 else bId
   }
}
