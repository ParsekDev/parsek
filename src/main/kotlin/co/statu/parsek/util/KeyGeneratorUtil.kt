package co.statu.parsek.util

import java.math.BigInteger
import java.security.SecureRandom

object KeyGeneratorUtil {
    fun generateSecretKey(): String {
        // Generate a 64-byte secret key
        val secretKey = ByteArray(64)
        SecureRandom().nextBytes(secretKey)

        // Convert the secret key to a hexadecimal string
        return BigInteger(1, secretKey).toString(16)
    }
}