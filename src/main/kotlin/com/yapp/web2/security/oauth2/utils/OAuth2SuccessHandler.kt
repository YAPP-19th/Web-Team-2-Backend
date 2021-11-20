package com.yapp.web2.security.oauth2.utils

import com.yapp.web2.security.jwt.JwtProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class OAuth2SuccessHandler(
    @Autowired val jwtProvider: JwtProvider
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
        val targetUrl = determineTargetUrl(request, response, authentication)
        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    override fun determineTargetUrl(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?): String {
        val token = authentication?.let { jwtProvider.createToken(it) }
        return UriComponentsBuilder.fromUriString("localhost:8080/")
            .queryParam("token", token)
            .build().toUriString()
    }
}