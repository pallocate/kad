package kad.routing

import java.util.TreeSet
import pen.Loggable

import pen.Config
import kad.node.KKeyComparator
import kad.node.KNode
import kad.node.KNodeId

/** A Kademlia routing table */
@Serializable
class KRoutingTable () : Loggable
{
   /** The buckets in this routing table */
   var node = KNode()
   private var buckets = createBuckets()

   init
   { log( "KRoutingTable created", Config.trigger( "KAD_CREATE" ), pen.LogLevel.INFO) }

   fun initialize (kNode : KNode)
   {
      node = kNode
      log("initializing", Config.trigger( "KAD_INITIALIZE" ))
      insert( node )                                                            // Insert the local node
   }

   private fun createBuckets () : Array<KBucket>
   {
      var i = 0
      val ret = Array<KBucket>( KNodeId.ID_SIZE, {var i = 0; KBucket( i++ )} )
      return ret
   }

   /** A List of all Nodes in this KRoutingTable */
   @Synchronized
   fun allNodes () : ArrayList<KNode>
   {
      val nodes = ArrayList<KNode>()

      for (b in buckets)
         for (c in b.contacts)
            nodes.add( c.node )

      return nodes
   }

   /** A List of all Contacts in this KRoutingTable */
   fun allContacts () : ArrayList<KContact>
   {
       val contacts = ArrayList<KContact>()

       for (b in buckets)
           contacts.addAll( b.contacts )

       return contacts
   }

   /** Adds a contact to the routing table based on how far it is from the LocalNode. */
   @Synchronized
   fun insert (kContact : KContact)
   {
      log("inserting contact (${kContact.node})", Config.trigger( "KAD_CONTACT_PUT" ))
      log("contact info: {address: ${kContact.node.inetAddress}}, {port: ${kContact.node.port}}", Config.trigger( "KAD_CONTACT_INFO" ))
      buckets[getBucketId( kContact.node.nodeId )].insert(kContact)
   }

   /** Adds a node to the routing table based on how far it is from the LocalNode. */
   @Synchronized
   fun insert (kNode : KNode)
   {
      log("inserting node (${kNode})", Config.trigger( "KAD_CONTACT_PUT" ))
      log("node info: {address: ${kNode.inetAddress}}, {port: ${kNode.port}}", Config.trigger( "KAD_CONTACT_INFO" ))
      buckets[getBucketId( kNode.nodeId )].insert(kNode)
   }

   /** Compute the bucket ID in which a given node should be placed; the bucketId is computed based on how far the node is away from the Local Node.
     * @param kNodeId The NodeID for which we want to find which bucket it belong to
     * @return Integer The bucket ID in which the given node should be placed. */
   fun getBucketId (kNodeId : KNodeId) : Int
   {
      val bId = node.nodeId.getDistance( kNodeId ) - 1

      /* If we are trying to insert a node into it's own routing table, then the bucket ID will be -1, so let's just keep it in bucket 0 */
      return if (bId < 0) 0 else bId
   }

   /** Find the closest set of contacts to a given NodeID
     * @param target The NodeID to find contacts close to
     * @param numNodesRequired The number of contacts to find
     * @return List A List of contacts closest to target */
   @Synchronized
   fun findClosest (target : KNodeId, numNodesRequired : Int) : ArrayList<KNode>
   {
      val sortedSet = TreeSet(KKeyComparator( target ))
      sortedSet.addAll( allNodes() )

      val closest = ArrayList<KNode>(numNodesRequired)

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

   override fun originName () = "KRoutingTable(${node})"

   @Synchronized
   override fun toString () : String
   {
      val sb = StringBuilder( " **********  Routing Table  **********\n" )
      var totalContacts = 0

      for (b in buckets)
         if (b.numContacts() > 0)
         {
            totalContacts += b.numContacts()
            sb.append( "# nodes in Bucket with depth " )
            sb.append( b.getDepth() )
            sb.append( ": " )
            sb.append( b.numContacts() )
            sb.append( "\n" )
            sb.append( b.toString() )
            sb.append( "\n" )
         }

      sb.append( "\nTotal Contacts: " )
      sb.append( totalContacts )
      sb.append( "\n\n" )
      sb.append( " **********  Routing Table  **********" )

      return sb.toString()
   }
}
