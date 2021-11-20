package com.yapp.web2.security.oauth2

import com.yapp.web2.domain.user.entity.Account
import com.yapp.web2.domain.user.repository.UserRepository
import com.yapp.web2.security.oauth2.utils.OAuth2UserInfoFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import kotlin.RuntimeException

@Service
class CustomOAuth2UserService(
    @Autowired val userRepository: UserRepository
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User? {
        val oAuth2User = super.loadUser(userRequest)
        val socialType = userRequest?.clientRegistration?.registrationId
            ?: throw RuntimeException("소셜 타입이 존재하지 않습니다. 회원가입이 불가능합니다")

        return processOAuth2User(socialType, oAuth2User)
    }

    @Transactional
    protected fun processOAuth2User(socialType: String, oAuth2User: OAuth2User): OAuth2User {
        val oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(socialType, oAuth2User.attributes)

        val account = (userRepository.findByEmail(oAuth2UserInfo.getEmail())?.let {
            if (it.socialType != socialType) throw RuntimeException("동일한 아이디가 다른 oauth로 존재하는 경우")
            it.image = oAuth2UserInfo.getImageUrl()
            it
        } ?: Account(oAuth2UserInfo.getEmail(), oAuth2UserInfo.getImageUrl(), oAuth2UserInfo.getName(), socialType))

        return oAuth2UserInfo
    }
}
