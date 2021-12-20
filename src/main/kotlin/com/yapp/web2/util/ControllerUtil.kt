package com.yapp.web2.util

import javax.servlet.http.HttpServletRequest

class ControllerUtil {
    companion object {
        private const val ACCESS_TOKEN = "AccessToken"
        private const val REFRESH_TOKEN = "RefreshToken"

        fun extractAccessToken(request: HttpServletRequest): String {
            return request.getHeader(ACCESS_TOKEN)
        }
        fun extractRefreshToken(request: HttpServletRequest): String {
            return request.getHeader(REFRESH_TOKEN)
        }
    }
}