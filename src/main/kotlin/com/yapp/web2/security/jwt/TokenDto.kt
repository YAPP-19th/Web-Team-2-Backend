package com.yapp.web2.security.jwt

data class TokenDto(
    val accessToken: String,
    val refreshToken: String
)