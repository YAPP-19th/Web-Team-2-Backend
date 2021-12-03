package com.yapp.web2.security.jwt

import com.yapp.web2.exception.custom.NoRefreshTokenException
import com.yapp.web2.exception.custom.TokenMisMatchException
import com.yapp.web2.domain.account.entity.Account
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Function

@Component
class JwtProvider(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.accessTokenExpiration}") private val accessTokenExpiration: Long,
    @Value("\${jwt.refreshTokenExpiration}") private val refreshTokenExpiration: Long,
    @Value("\${jwt.redis.expiration}") private val redisExpiration: Long,
    @Autowired private val redisTemplate: RedisTemplate<String, Any>
) {
    companion object {
        const val BEARER_PREFIX = "Bearer "
    }

    fun getAuthentication(token: String): Authentication {
        val authority = arrayListOf(SimpleGrantedAuthority("USER"))
        return UsernamePasswordAuthenticationToken(getIdFromToken(token), "", authority)
    }

    fun createToken(account: Account): TokenDto {
        val id = account.id!!.toString()
        val accessToken: String = createAccessToken(id)
        val refreshToken: String = createRefreshToken(id)

        return TokenDto(accessToken, refreshToken)
    }

    fun reIssuedAccessToken(accessToken: String, refreshToken: String): TokenDto {
        val refreshToken = removePrefix(refreshToken)
        val idFromToken = getIdFromToken(refreshToken).toString()
        val refreshTokenInRedis = getRefreshTokenInRedis(idFromToken)
            ?: throw NoRefreshTokenException()

        if (!isRefreshTokensSame(refreshToken, refreshTokenInRedis)) throw TokenMisMatchException()

        return TokenDto(createAccessToken(idFromToken), refreshToken)
    }

    private fun isRefreshTokensSame(receivedRefreshToken: String, existRefreshToken: String) = receivedRefreshToken == existRefreshToken

    private fun getRefreshTokenInRedis(idFromToken: String): String? {
        return redisTemplate.opsForValue().get(idFromToken) as? String
    }

    private fun createAccessToken(id: String): String {
        val expiration = Date()
        expiration.time += accessTokenExpiration

        return Jwts.builder()
            .setSubject(id)
            .setIssuedAt(Date())
            .setExpiration(expiration)
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact()
    }

    private fun createRefreshToken(id: String): String {
        val expiration = Date()
        expiration.time += refreshTokenExpiration

        val refreshToken = Jwts.builder()
            .setSubject(id)
            .setIssuedAt(Date())
            .setExpiration(expiration)
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact()

        saveRefreshToken(refreshToken, id)
        return refreshToken
    }

    private fun saveRefreshToken(compact: String, id: String) {
        redisTemplate.opsForValue().set(id, compact, redisExpiration, TimeUnit.DAYS)
    }

    private fun getAllClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .body
    }

    fun getExpirationDateFromToken(token: String): Date {
        return getClaimFromToken(token) { obj: Claims -> obj.expiration }
    }

    fun getIdFromToken(token: String): Long {
        return getClaimFromToken(removePrefix(token)) { obj: Claims -> obj.subject }.toLong()
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

    private fun removePrefix(token: String) = token.removePrefix(BEARER_PREFIX)
}