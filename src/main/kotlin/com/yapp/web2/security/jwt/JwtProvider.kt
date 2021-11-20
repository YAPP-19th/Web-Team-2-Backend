package com.yapp.web2.security.jwt

import com.yapp.web2.security.oauth2.dto.OAuth2UserInfo
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.*
import java.util.function.Function


@Component
class JwtProvider(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.expiration}") private val expiration: Long
) {

    fun createToken(authentication: Authentication): String {
        val userInfo = authentication.principal as OAuth2UserInfo

        println(userInfo.getEmail())
        val now = Date()
        val expire = Date(expiration)

        return Jwts.builder()
            .setSubject(userInfo.getEmail())
            .setIssuedAt(now)
            .setExpiration(expire)
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact()
    }

    private fun getAllClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .body
    }

    fun getExpirationDateFromToken(token: String): Date {
        return getClaimFromToken(token, Function { obj: Claims -> obj.expiration })
    }

    fun getEmailFromToken(token: String): String? {
        return getClaimFromToken(token, Function { obj: Claims -> obj.subject })
    }

    fun <T> getClaimFromToken(token: String, claimsResolver: Function<Claims, T>): T {
        val claims: Claims = getAllClaimsFromToken(token)
        return claimsResolver.apply(claims)
    }

    fun validateToken(token: String): Boolean {
        return isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        val expiration = getExpirationDateFromToken(token)
        return expiration.before(Date())
    }

}