package com.sms.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.util.*
import javax.crypto.SecretKey

object JwtUtil {
    private val secretKey: SecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)
    private const val EXPIRATION_MS: Long = 3600000 // 1 hour

    fun generateToken(username: String, roles: List<String>): String {
        val now = Date()
        val expiryDate = Date(now.time + EXPIRATION_MS)

        return Jwts.builder()
            .setSubject(username)
            .claim("roles", roles)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    fun getClaims(token: String): Claims? {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (ex: Exception) {
            null
        }
    }

    fun extractUsername(token: String): String? {
        return getClaims(token)?.subject
    }

    fun extractRoles(token: String): List<String> {
        return getClaims(token)
            ?.get("roles", List::class.java)
            ?.map { it.toString() }
            ?: emptyList()
    }

    fun validateToken(token: String): Boolean {
        val claims = getClaims(token) ?: return false
        return claims.expiration.after(Date())
    }
}