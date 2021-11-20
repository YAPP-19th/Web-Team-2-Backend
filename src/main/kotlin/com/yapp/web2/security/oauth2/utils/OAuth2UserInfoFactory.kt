package com.yapp.web2.security.oauth2.utils

import com.yapp.web2.security.oauth2.dto.GoogleUserInfo
import com.yapp.web2.security.oauth2.dto.OAuth2UserInfo

class OAuth2UserInfoFactory {
    companion object {
        fun getOAuth2UserInfo(registrationId: String, attributes: Map<String, Any>): OAuth2UserInfo {
            when (registrationId.lowercase()) {
                "google" -> return GoogleUserInfo(attributes)
            }
            // 존재하지 않는 OAuth2에 대한 exception 만들기
            throw RuntimeException()
        }
    }
}