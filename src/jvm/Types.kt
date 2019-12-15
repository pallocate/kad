package kad

import java.io.IOException
import com.beust.klaxon.Converter

interface NodeMessageListener
{
   fun findMessageSent ()
   fun findReplyReceived ()
}
class NoNodeMessageListener : NodeMessageListener
{
   override fun findMessageSent () {}
   override fun findReplyReceived () {}
}

interface StorageEntry
class NoStorageEntry : StorageEntry

interface StorageEntryMetadata
class NoStorageEntryMetadata : StorageEntryMetadata

/* Exceptions */
class ContentNotFoundException : Exception
{
    constructor() : super() {}
    constructor(message: String) : super(message) {}
}

open class RoutingException : IOException
{
    constructor() : super() {}
    constructor(message: String) : super(message) {}
}
