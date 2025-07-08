package com.sms.security

import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtTokenProvider(
    @Value("\${app.jwt.secret}")
    private val jwtSecret: String,

    @Value("\${app.jwt.expiration-in-ms}")
    private val jwtExpirationInMs: Int
) {
    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    fun generateToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserPrincipal
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationInMs)

        return Jwts.builder()
            .setSubject(userPrincipal.id.toString())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun getUserIdFromToken(token: String): Long {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .body

        return claims.subject.toLong()
    }

    fun validateToken(authToken: String): Boolean {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken)
            return true
        } catch (ex: MalformedJwtException) {
            logger.error("Invalid JWT token")
        } catch (ex: ExpiredJwtException) {
            logger.error("Expired JWT token")
        } catch (ex: UnsupportedJwtException) {
            logger.error("Unsupported JWT token")
        } catch (ex: IllegalArgumentException) {
            logger.error("JWT claims string is empty")
        }
        return false
    }

    private fun key(): Key {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))
    }
}