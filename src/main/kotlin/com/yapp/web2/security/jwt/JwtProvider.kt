package com.yapp.web2.security.jwt

import com.yapp.web2.domain.user.entity.Account
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.*
import java.util.function.Function


@Component
class JwtProvider(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.accessTokenExpiration}") private val accessTokenExpiration: Long,
    @Value("\${jwt.refreshTokenExpiration}") private val refreshTokenExpiration: Long
) {

    fun getAuthentication(token: String): Authentication {
        val authority = arrayListOf(SimpleGrantedAuthority("USER"))
        return UsernamePasswordAuthenticationToken(getIdFromToken(token), "", authority)
    }

    fun createToken(account: Account): TokenDto {
        val id = account.id!!
        var accessToken: String = createAccessToken(id)
        var refreshToken: String = createRefreshToken(id)

        return TokenDto(accessToken, refreshToken)
    }

    private fun createAccessToken(id: Long): String {
        val expiration = Date()
        expiration.time += accessTokenExpiration

        return Jwts.builder()
            .setSubject(id.toString())
            .setIssuedAt(Date())
            .setExpiration(expiration)
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact()
    }

    private fun createRefreshToken(id: Long): String {
        val expiration = Date()
        expiration.time += refreshTokenExpiration

        return Jwts.builder()
            .setSubject(id.toString())
            .setIssuedAt(Date())
            .setExpiration(expiration)
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

    fun getIdFromToken(token: String): Long {
        return getClaimFromToken(token, Function { obj: Claims -> obj.subject }).toLong()
    }

    fun <T> getClaimFromToken(token: String, claimsResolver: Function<Claims, T>): T {
        val claims: Claims = getAllClaimsFromToken(token)
        return claimsResolver.apply(claims)
    }

    fun validateToken(token: String): Boolean {
        return isTokenExpired(token)
    }

    //여기서 예외를 터뜨려야할 거 같습니다? status에 expired됐다는 표시를 남겨야할듯
    private fun isTokenExpired(token: String): Boolean {
        val expiration = getExpirationDateFromToken(token)
        return expiration.before(Date())
    }

}