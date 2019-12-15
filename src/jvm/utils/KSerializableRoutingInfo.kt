package kad.utils

import pen.Filable
import kad.node.KNode
import kad.routing.KRoutingTable
import kad.routing.KContact

class KSerializableRoutingInfo () : Filable
{
   var localNodeInfo                              = KNode()
   var contactsInfo                               = ArrayList<KContact>()

   constructor (routingTable : KRoutingTable) : this()
   {
      localNodeInfo = routingTable.node
      contactsInfo = routingTable.allContacts()
   }

   fun toRoutingTable () : KRoutingTable
   {
      val kRoutingTable = KRoutingTable()

      kRoutingTable.node = localNodeInfo
      for (contact in contactsInfo)
         kRoutingTable.insert( contact )

      return kRoutingTable
   }
}
