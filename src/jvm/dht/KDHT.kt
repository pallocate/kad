package kad.dht

import java.io.File
import java.util.NoSuchElementException
import kotlinx.serialization.Serializable
import pen.LogLevel.WARN
import pen.Loggable
import pen.Config
import pen.serializeToFile
import pen.deserializeFromFile
import pen.Constants.SLASH
import pen.Constants.JSON_EXTENSION
import kad.KKademliaNode
import kad.StorageEntry
import kad.NoStorageEntry
import kad.ContentNotFoundException
import kad.node.KNodeId

/** Distributed Hash Table implementation. */
@Serializable
class KDHT () : Loggable
{
   val contentManager = KContentManager()
   var ownerName = ""

   init
   { log("created", Config.trigger( "KAD_CREATE" )) }

   fun initialize () = log("initializing", Config.trigger( "KAD_INITIALIZE" ))

   fun store (content : KStorageEntry) : Boolean
   {
      log("adding content [${content.contentMetadata.nodeId.shortName()}]", Config.trigger( "KAD_CONTENT_PUT_GET" ))
      var ret : Boolean

      if (contentManager.contains( content.contentMetadata ))
      {
         val current = contentManager.get( content.contentMetadata )
         if (current is KStorageEntryMetadata)
         {
            current.updateLastRepublished()

            if (current.lastUpdated >= content.contentMetadata.lastUpdated)
               return false                                                     // Content is up to date
            else
               remove( content.contentMetadata )                                // Delete content in order update it
         }
      }

      /** Adds new or updated content. */
      try
      {
         val sEntry = contentManager.put( content.contentMetadata )

         val filename = contentStorageDir( content.contentMetadata.nodeId ) + SLASH + sEntry.hashCode() + JSON_EXTENSION
         serializeToFile<KStorageEntry>( content, filename, KStorageEntry.serializer() )                                           // Write content to file

         ret = true
      }
      catch (e: Exception)
      {
         log("adding content failed! [${content.contentMetadata.nodeId.shortName()}], ${e.message}", Config.trigger( "KAD_CONTENT_PUT_GET" ), WARN)
         ret = false
      }

      return ret
   }

   fun store (content : KContent) = store(KStorageEntry( content ))

   fun retrieve (nodeId : KNodeId, hashCode : Int) : StorageEntry
   {
      var ret : StorageEntry = NoStorageEntry()

      val filename = contentStorageDir( nodeId ) + SLASH + hashCode + JSON_EXTENSION
      val readResult = deserializeFromFile<KStorageEntry>( filename, KStorageEntry.serializer() )

      if (readResult is StorageEntry)
         ret = readResult

      return ret
   }

   fun contains (param : KGetParameter) = contentManager.contains( param )

   fun get (entry : KStorageEntryMetadata) : StorageEntry
   {
      var ret : StorageEntry = NoStorageEntry()

      val storageEntry = retrieve( entry.nodeId, entry.hashCode() )
      if (storageEntry is KStorageEntry)
         ret = storageEntry

      return ret
   }

   /* Loads content if any. */
   fun get (param : KGetParameter) : StorageEntry
   {
      var ret : StorageEntry = NoStorageEntry()

      val meta = contentManager.get( param )
      if (meta is KStorageEntryMetadata)
      {
         val storageEntry = retrieve( meta.nodeId, meta.hashCode() )
         if (storageEntry is KStorageEntry)
            ret = storageEntry
      }

      return ret
   }

   fun remove (content : KContent)
   { remove(KStorageEntryMetadata( content )) }

   fun remove (entry : KStorageEntryMetadata)
   {
      val folder = contentStorageDir( entry.nodeId )
      val file = File(folder + SLASH + entry.hashCode() + ".json" )

      contentManager.remove( entry )

      if (file.exists())
         file.delete()
      else
         throw ContentNotFoundException()
   }

   /** The first 2 characters of the content id is used as the directory name. */
   private fun contentStorageDir (nodeId : KNodeId) = KKademliaNode.storageDir(ownerName, nodeId.toString().substring( 0, 2 ))

   fun getStorageEntries () = contentManager.allEntries()
   fun putStorageEntries (ientries : MutableList<KStorageEntryMetadata>)
   {
      for (e in ientries)
         contentManager.put( e )
   }

   override fun tag () = "KDHT(${ownerName})"

   @Synchronized
   override fun toString () = contentManager.toString()
}
