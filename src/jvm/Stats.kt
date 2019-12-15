package kad

import java.text.DecimalFormat

/** Statistics. */
object Stats
{
   private var totalDataSent                      = 0L
   private var totalDataReceived                  = 0L
   private var bootstrapTime                      = 0L
   private var totalRouteLength                   = 0L
   private var numDataSent                        = 0L
   private var numDataReceived                    = 0L


   var numContentLookups : Int = 0
      private set
   var numFailedContentLookups : Int = 0
      private set
   var totalContentLookupTime : Long = 0
      private set

   fun sentData (size : Long)
   {
      totalDataSent += size
      numDataSent++
   }

   fun receivedData (size : Long)
   {
      totalDataReceived += size
      numDataReceived++
   }

   fun addContentLookup (time : Long, routeLength : Int, isSuccessful : Boolean)
   {
      if (isSuccessful)
      {
         numContentLookups++
         totalContentLookupTime += time
         totalRouteLength += routeLength.toLong()
      }
      else
      { numFailedContentLookups++ }
   }

   fun averageContentLookupTime () : Double
   {
      var ret = 0.0

      if (this.numContentLookups != 0)
      {
         val avg = totalContentLookupTime.toDouble()/numContentLookups.toDouble()/1000000.0
         val df = DecimalFormat( "#.00" )
         ret = df.format( avg ).toDouble()
      }

      return ret
   }


   fun averageContentLookupRouteLength () : Double
   {
      if (numContentLookups == 0)
         return 0.0

      val avg = totalRouteLength.toDouble()/numContentLookups.toDouble()
      val df = DecimalFormat( "#.00" )

      return df.format( avg ).toDouble()
   }

   fun getTotalDataSent () = totalDataSent/1000L
   fun getTotalDataReceived () = totalDataReceived/1000L
   fun getBootstrapTime () = bootstrapTime/1000000L
   fun setBootstrapTime (bootstrapTime : Long)
   { this.bootstrapTime = bootstrapTime }

   override fun toString () : String
   {
      val sb = StringBuilder( "  STATS\n\n" )

      sb.append( "Bootstrap Time: $bootstrapTime\n\n" )
      sb.append( "Data Sent: ($numDataSent) $totalDataSent bytes\n\n" )
      sb.append( "Data Received: ($numDataReceived) $totalDataReceived bytes\n\n" )
      sb.append( "Num Content Lookups: $numContentLookups\n\n")
      sb.append( "Avg Content Lookup Time: ${averageContentLookupTime()}\n\n" )
      sb.append( "Avg Content Lookup Route Lth: ${averageContentLookupRouteLength()}" )

      return sb.toString()
   }
}
