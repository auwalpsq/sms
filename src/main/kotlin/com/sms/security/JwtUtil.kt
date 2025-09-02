package com.sms.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.util.*
import javax.crypto.SecretKey

object JwtUtil {
    private const val SECRET = "testing_testing_testing_testing_testing" // 256-bit key
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(SECRET.toByteArray())
    private const val EXPIRATION_MS: Long = 3600000 // 1 hour

    fun generateToken(username: String, roles: List<String>): String {
        val now = Date()
        val expiry = Date(now.time + EXPIRATION_MS)
        return Jwts.builder()
            .setSubject(username)
            .claim("roles", roles)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(secretKey)
            .compact()
    }

    fun extractUsername(token: String): String? =
        try { Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).body.subject }
        catch (_: Exception) { null }

    fun extractRoles(token: String): List<String> =
        try {
            val claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).body
            @Suppress("UNCHECKED_CAST")
            (claims["roles"] as? List<String>) ?: emptyList()
        } catch (_: Exception) { emptyList() }

    fun validateToken(token: String): Boolean =
        try {
            val claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).body
            claims.expiration.after(Date())
        } catch (_: Exception) { false }
}