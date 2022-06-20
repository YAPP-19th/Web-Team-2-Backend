package com.yapp.web2.util

enum class CustomStatusCode(
    val code: Int
) {
    NO_REFRESH_TOKEN(401);

    companion object {
    }
}