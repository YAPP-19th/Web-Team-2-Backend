package com.yapp.web2.domain.user.service

import com.yapp.web2.domain.user.entity.Account
import com.yapp.web2.domain.user.repository.UserRepository
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.security.jwt.TokenDto
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider
) {
    fun oauth2LoginUser(dto: Account.AccountRequest): TokenDto {
        val account = Account.requestToAccount(dto)

        when (val savedAccount = userRepository.findByEmail(account.email)) {
            null -> createUser(account)
            else -> updateUser(savedAccount, account)
        }

        return jwtProvider.createToken(account)
    }

    @Transactional
    protected fun updateUser(savedAccount: Account, receivedAccount: Account) {
        savedAccount.image = receivedAccount.image
        savedAccount.nickname = receivedAccount.nickname
    }

    @Transactional
    fun createUser(account: Account): Account {
        return userRepository.save(account)
    }

    fun reIssuedAccessToken(accessToken: String, refreshToken: String): TokenDto {
        return jwtProvider.reIssuedAccessToken(accessToken, refreshToken)
    }
}