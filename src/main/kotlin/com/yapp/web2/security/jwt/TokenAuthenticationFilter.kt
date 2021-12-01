package com.yapp.web2.security.jwt

import com.yapp.web2.exception.custom.PrefixMisMatchException
import com.yapp.web2.util.Message
import io.jsonwebtoken.ExpiredJwtException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class TokenAuthenticationFilter(
    @Autowired private val jwtProvider: JwtProvider
) : GenericFilterBean() {

    companion object {
        const val AUTHORIZATION_HEADER: String = "Access-Token"
        const val BEARER_PREFIX = "Bearer "
    }

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        try {
            val token = resolveToken(request as HttpServletRequest)
            response as HttpServletResponse

            if (!jwtProvider.validateToken(token))
                SecurityContextHolder.getContext().authentication = jwtProvider.getAuthentication(token)
            chain!!.doFilter(request, response)
        } catch (e: ExpiredJwtException) {
            response!!.contentType = "application/json;charset=UTF-8"
            response.writer.println(Message.ACCESS_TOKEN_EXPIRED)
        } catch (e: PrefixMisMatchException) {
            response!!.contentType = "application/json;charset=UTF-8"
            response.writer.println("prefix_mismatch")
        }
    }


    private fun resolveToken(request: HttpServletRequest): String {
        val bearerToken: String = request.getHeader(AUTHORIZATION_HEADER)
        if (!bearerToken.startsWith(BEARER_PREFIX)) throw PrefixMisMatchException()
        return bearerToken.removePrefix(BEARER_PREFIX)
    }
}