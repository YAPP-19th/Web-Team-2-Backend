package com.yapp.web2.security.oauth2.utils

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class OAuth2FailureHandler : SimpleUrlAuthenticationFailureHandler() {
    override fun onAuthenticationFailure(request: HttpServletRequest?, response: HttpServletResponse?, exception: AuthenticationException?) {
        var targetUrl = UriComponentsBuilder.fromUriString("localhost:8080/")
            .queryParam("error", exception.toString())
            .build().toUriString()

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}