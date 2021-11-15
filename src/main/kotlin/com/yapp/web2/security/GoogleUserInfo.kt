package com.yapp.web2.security

data class GoogleUserInfo(
    val attributes: Map<String, Any>
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

}
