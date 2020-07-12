package kad

import java.io.IOException

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
