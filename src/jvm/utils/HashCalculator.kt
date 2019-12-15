package kad.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * A class that is used to calculate the hash of strings.
 *
 * @author Joshua Kissoon
 * @since 20140405
 */
object HashCalculator {

    /**
     * Computes the SHA-1 Hash.
     *
     * @param toHash The string to hash
     *
     * @return byte[20] The hashed string
     *
     * @throws java.security.NoSuchAlgorithmException
     */
    @Throws(NoSuchAlgorithmException::class)
    fun sha1Hash(toHash: String): ByteArray {
        /* Create a MessageDigest */
        val md = MessageDigest.getInstance("SHA-1")

        /* Add password bytes to digest */
        md.update(toHash.toByteArray())

        /* Get the hashed bytes */
        return md.digest()
    }

    /**
     * Computes the SHA-1 Hash using a Salt.
     *
     * @param toHash The string to hash
     * @param salt   A salt used to blind the hash
     *
     * @return byte[20] The hashed string
     *
     * @throws java.security.NoSuchAlgorithmException
     */
    @Throws(NoSuchAlgorithmException::class)
    fun sha1Hash(toHash: String, salt: String): ByteArray {
        /* Create a MessageDigest */
        val md = MessageDigest.getInstance("SHA-1")

        /* Add password bytes to digest */
        md.update(toHash.toByteArray())

        /* Get the hashed bytes */
        return md.digest(salt.toByteArray())
    }

    /**
     * Computes the MD5 Hash.
     *
     * @param toHash The string to hash
     *
     * @return byte[16] The hashed string
     *
     * @throws java.security.NoSuchAlgorithmException
     */
    @Throws(NoSuchAlgorithmException::class)
    fun md5Hash(toHash: String): ByteArray {
        /* Create a MessageDigest */
        val md = MessageDigest.getInstance("MD5")

        /* Add password bytes to digest */
        md.update(toHash.toByteArray())

        /* Get the hashed bytes */
        return md.digest()
    }

    /**
     * Computes the MD5 Hash using a salt.
     *
     * @param toHash The string to hash
     * @param salt   A salt used to blind the hash
     *
     * @return byte[16] The hashed string
     *
     * @throws java.security.NoSuchAlgorithmException
     */
    @Throws(NoSuchAlgorithmException::class)
    fun md5Hash(toHash: String, salt: String): ByteArray {
        /* Create a MessageDigest */
        val md = MessageDigest.getInstance("MD5")

        /* Add password bytes to digest */
        md.update(toHash.toByteArray())

        /* Get the hashed bytes */
        return md.digest(salt.toByteArray())
    }
}
