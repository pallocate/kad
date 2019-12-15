package kad.operations

/** An operation in the Kademlia routing protocol */
interface Operation
{
   /** Starts an operation and returns when the operation is finished */
   fun execute ()
}
