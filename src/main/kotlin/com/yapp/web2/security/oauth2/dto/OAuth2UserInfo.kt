package com.yapp.web2.security.oauth2.dto

import org.springframework.security.oauth2.core.user.OAuth2User

abstract class OAuth2UserInfo(
    attributes: Map<String, Any>
) : OAuth2User {
    abstract fun getEmail(): String
    abstract fun getImageUrl(): String
    abstract fun getId(): String
    abstract override fun getName(): String
}