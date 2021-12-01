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

        account = when (val savedAccount = userRepository.findByEmail(account.email)) {
            null -> createUser(account)
            else -> updateUser(savedAccount, account)
        }

        return jwtProvider.createToken(account)
    }

    @Transactional
    protected fun updateUser(savedAccount: Account, receivedAccount: Account): Account {
        savedAccount.image = receivedAccount.image
        savedAccount.nickname = receivedAccount.nickname
        return savedAccount
    }

    @Transactional
    fun createUser(account: Account): Account {
        return userRepository.save(account)
    }

    fun reIssuedAccessToken(accessToken: String, refreshToken: String): TokenDto {
        return jwtProvider.reIssuedAccessToken(accessToken, refreshToken)
    }
}