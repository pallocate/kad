package kad

import java.text.DecimalFormat

/** Statistics. */
interface Stats
{
   var findNodeSent : Int
   var findReplyReceived : Int
   var packetsSent : Int
   var packetsReceived : Int
   var bytesSent : Long
   var bytesReceived : Long

   fun contentLookup (time : Long, routeLength : Int, isSuccessful : Boolean)
   fun setBootstrapTime (bootstrapTime : Long)
}
class NoStats : Stats
{
   override var findNodeSent = 0
   override var findReplyReceived = 0
   override var packetsSent = 0
   override var packetsReceived = 0
   override var bytesSent = 0L
   override var bytesReceived = 0L
   override fun contentLookup (time : Long, routeLength : Int, isSuccessful : Boolean) {}
   override fun setBootstrapTime (bootstrapTime : Long) {}
}

class KStats () : Stats
{
   override var findNodeSent                           = 0
   override var findReplyReceived                      = 0
   private var totalRouteLength                        = 0L
   override var packetsSent                            = 0
   override var packetsReceived                        = 0
   override var bytesSent                              = 0L
   override var bytesReceived                          = 0L
   private var bootstrapTime                           = 0L

   var contentLookups : Int = 0
      private set
   var numFailedContentLookups : Int = 0
      private set
   var totalContentLookupTime : Long = 0
      private set

   override fun contentLookup (time : Long, routeLength : Int, isSuccessful : Boolean)
   {
      if (isSuccessful)
      {
         contentLookups++
         totalContentLookupTime += time
         totalRouteLength += routeLength.toLong()
      }
      else
      { numFailedContentLookups++ }
   }

   fun averageContentLookupTime () : Double
   {
      var ret = 0.0

      if (this.contentLookups != 0)
      {
         val avg = totalContentLookupTime.toDouble()/contentLookups.toDouble()/1000000.0
         val df = DecimalFormat( "#.00" )
         ret = df.format( avg ).toDouble()
      }

      return ret
   }


   fun averageContentLookupRouteLength () : Double
   {
      if (contentLookups == 0)
         return 0.0

      val avg = totalRouteLength.toDouble()/contentLookups.toDouble()
      val df = DecimalFormat( "#.00" )

      return df.format( avg ).toDouble()
   }

   override fun setBootstrapTime (bootstrapTime : Long)
   { this.bootstrapTime = bootstrapTime }
   fun getBootstrapTime () = bootstrapTime/1000000L

   override fun toString () : String
   {
      val sb = StringBuilder()

      sb.append( "bootstrap time: $bootstrapTime\n" )
      sb.append( "sent/recieved: ${bytesSent}B/${bytesReceived}B\n" )
      sb.append( "find nodes sent: ${findNodeSent}\n" )
      sb.append( "find replies recieved: ${findReplyReceived}\n" )

      sb.append( "content lookups: $contentLookups\n")
      sb.append( " average time; ${averageContentLookupTime()}\n" )
      sb.append( " average route length; ${averageContentLookupRouteLength()}" )

      return sb.toString()
   }
}
