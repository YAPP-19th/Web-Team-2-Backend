package com.yapp.web2.security.jwt

import com.yapp.web2.exception.custom.PrefixMisMatchException
import com.yapp.web2.util.Message
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
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
        const val AUTHORIZATION_HEADER: String = "AccessToken"
        const val BEARER_PREFIX = "Bearer "
    }

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        val httpServletResponse = response as HttpServletResponse
        try {
            val token = resolveToken(request as HttpServletRequest)
            if (!jwtProvider.validateToken(token))
                SecurityContextHolder.getContext().authentication = jwtProvider.getAuthentication(token)
            chain!!.doFilter(request, response)
        } catch (e: ExpiredJwtException) {
            httpServletResponse.contentType = "application/json;charset=UTF-8"
            httpServletResponse.status = HttpStatus.UNAUTHORIZED.value()
            httpServletResponse.writer.println(Message.ACCESS_TOKEN_EXPIRED)
        } catch (e: PrefixMisMatchException) {
            httpServletResponse.contentType = "application/json;charset=UTF-8"
            httpServletResponse.status = HttpStatus.UNAUTHORIZED.value()
            httpServletResponse.writer.println(Message.PREFIX_MISMATCH)
        } catch (e: MalformedJwtException) {
            httpServletResponse.contentType = "application/json;charset=UTF-8"
            httpServletResponse.status = HttpStatus.UNAUTHORIZED.value()
            httpServletResponse.writer.println(Message.WRONG_TOKEN_FORM)
        } catch (e: NullPointerException) {
            httpServletResponse.contentType = "application/json;charset=UTF-8"
            httpServletResponse.status = HttpStatus.UNAUTHORIZED.value()
            httpServletResponse.writer.println(Message.NULL_TOKEN)
        }
    }

    private fun resolveToken(request: HttpServletRequest): String {
        val bearerToken: String = request.getHeader(AUTHORIZATION_HEADER)
        if (!bearerToken.startsWith(BEARER_PREFIX)) throw PrefixMisMatchException()
        return bearerToken.removePrefix(BEARER_PREFIX)
    }
}