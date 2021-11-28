package com.yapp.web2.domain.account.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.UserRepository
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.security.jwt.TokenDto
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider
) {

    fun oauth2LoginUser(dto: Account.AccountLoginRequest): TokenDto {
        var account = Account.requestToAccount(dto)
        // 존재할 수도 있다
        account = userRepository.findByEmail(account.email)?.let {
            if (it.socialType != account.socialType) throw RuntimeException("동일한 아이디가 다른 oauth로 존재하는 경우")
            it.image = account.image
            it
        } ?: account

        createUser(account)

        return jwtProvider.createToken(account)
    }

    @Transactional
    fun createUser(account: Account): Account {
        return userRepository.save(account)
    }
}