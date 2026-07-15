package rw.mtn.ussd.admin

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtil(
    @Value("\${admin.jwt-secret:}") private val secret: String,
    @Value("\${admin.jwt-expiration-hours:10}") 
    private val expirationHours: Long
) {

    private val key: SecretKey? by lazy {
        if (secret.isBlank()) null
        else {
            val bytes = secret.toByteArray(Charsets.UTF_8)
            val padded = if (bytes.size >= 32) bytes
                         else bytes.copyOf(32)
            Keys.hmacShaKeyFor(padded)
        }
    }

    fun isConfigured(): Boolean = key != null

    fun generateToken(username: String): String {
        val signingKey = key ?: error("admin.jwt-secret is not configured")
        val now = Date()
        val expiry = Date(now.time + expirationHours * 3_600_000L)
        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact()
    }

    fun validateAndGetSubject(token: String): String? {
        val signingKey = key ?: return null
        return try {
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
        } catch (ex: Exception) {
            null
        }
    }
}
