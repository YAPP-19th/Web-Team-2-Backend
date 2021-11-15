package com.yapp.web2.security

abstract class OAuth2UserInfo(
    attributes: Map<String, Any>
) {
    abstract fun getEmail(): String
    abstract fun getImageUrl(): String
    abstract fun getId(): String
}