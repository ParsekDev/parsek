package co.statu.parsek.util

import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest

object HashUtil {
    fun InputStream.hash() =
        String.format("%064x", BigInteger(1, MessageDigest.getInstance("SHA-256").digest(this.readBytes())))
}