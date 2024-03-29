package com.yapp.web2.security.jwt

import com.yapp.web2.exception.custom.PrefixMisMatchException
import com.yapp.web2.util.Message
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import org.slf4j.LoggerFactory
import net.minidev.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TokenAuthenticationFilter(
    @Autowired private val jwtProvider: JwtProvider
) : GenericFilterBean() {

    companion object {
        const val AUTHORIZATION_ACCESS_HEADER: String = "AccessToken"
        const val AUTHORIZATION_REFRESH_HEADER: String = "RefreshToken"
        const val BEARER_PREFIX = "Bearer "
        const val MESSAGE = "message"
        const val ERRORS = "errors"
        const val CONTENT_TYPE = "application/json;charset=UTF-8"
        private val log = LoggerFactory.getLogger(TokenAuthenticationFilter::class.java)
    }

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        val httpServletResponse = response as HttpServletResponse
        try {
            val accessToken = resolveAccessToken(request as HttpServletRequest)
            if (isRefreshToken(request)) {
                val refreshToken = resolveRefreshToken(request)
                jwtProvider.validateToken(refreshToken)
            }
            if (!jwtProvider.validateToken(accessToken))
                SecurityContextHolder.getContext().authentication = jwtProvider.getAuthentication(accessToken)

            chain!!.doFilter(request, response)
        } catch (e: ExpiredJwtException) {
            setResponse(httpServletResponse, Message.TOKEN_EXPIRED, HttpStatus.UNAUTHORIZED.value())
            log.debug("Jwt Expired: {}", e.message)
        } catch (e: PrefixMisMatchException) {
            setResponse(httpServletResponse, Message.PREFIX_MISMATCH, HttpStatus.UNAUTHORIZED.value())
            log.debug("Jwt Prefix MisMatch: {}", e.message)
        } catch (e: MalformedJwtException) {
            setResponse(httpServletResponse, Message.WRONG_TOKEN_FORM, HttpStatus.UNAUTHORIZED.value())
            log.debug("Jwt Wrong Configuration: {}", e.message)
        } catch (e: NullPointerException) {
            setResponse(httpServletResponse, Message.NULL_TOKEN, HttpStatus.UNAUTHORIZED.value())
        }
    }

    private fun resolveRefreshToken(request: HttpServletRequest): String {
        val bearerToken: String = request.getHeader(AUTHORIZATION_REFRESH_HEADER)
        if (!bearerToken.startsWith(BEARER_PREFIX)) throw PrefixMisMatchException()
        return bearerToken.removePrefix(BEARER_PREFIX)
    }

    private fun isRefreshToken(request: HttpServletRequest): Boolean {
        val token = request.getHeader(AUTHORIZATION_REFRESH_HEADER)
        if (token.isNullOrBlank()) return false
        return true
    }

    private fun resolveAccessToken(request: HttpServletRequest): String {
        val bearerToken: String = request.getHeader(AUTHORIZATION_ACCESS_HEADER)
        if (!bearerToken.startsWith(BEARER_PREFIX)) throw PrefixMisMatchException()
        return bearerToken.removePrefix(BEARER_PREFIX)
    }

    private fun setResponse(response: HttpServletResponse, message: String, code: Int) {
        val responseJson = JSONObject()
        responseJson[MESSAGE] = message
        responseJson[ERRORS] = emptyList<String>()

        response.status = code
        response.contentType = CONTENT_TYPE
        response.writer.print(responseJson)
    }
}