package kad

/* Networking constants */
object Constants
{
   val USER_HOME                                  = System.getProperty( "user.home" )
   val IS_TESTING                                 = true                        // Affects the servers simulated latency
   const val RESTORE_INTERVAL                     = (60*1000).toLong()          // (milliseconds)
   const val RESPONCE_TIMEOUT                     = 2000L
   const val OPERATION_TIMEOUT                    = 2000L
   const val MAX_CONCURRENT_MESSAGES_TRANSITING   = 10
   const val K                                    = 5
   const val STALE                                = 1
//   const val REPLACEMENT_CACHE_SIZE               = 3
}
