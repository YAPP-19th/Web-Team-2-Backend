package com.yapp.web2.security

import com.yapp.web2.domain.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService(
    @Autowired val userRepository: UserRepository
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val socialType = userRequest?.clientRegistration?.registrationId

        // 회원 조회하고 없으면 저장, 있으면 업데이트
        println("$oAuth2User")


        return super.loadUser(userRequest)
    }
}
