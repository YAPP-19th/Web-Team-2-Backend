package com.yapp.web2.security.oauth2.dto

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

data class GoogleUserInfo(
    private val attributes: Map<String, Any>
) : OAuth2UserInfo(attributes) {

    override fun getEmail(): String {
        return attributes["email"] as String
    }

    override fun getImageUrl(): String {
        return attributes["picture"] as String
    }

    override fun getId(): String {
        return attributes["sub"] as String
    }

    override fun getName(): String {
        return attributes["name"] as String
    }

    override fun getAttributes(): MutableMap<String, Any> {
        return attributes.toMutableMap()
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return Collections.singletonList(SimpleGrantedAuthority("USER"))
    }
}
