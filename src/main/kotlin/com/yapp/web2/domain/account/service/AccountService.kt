package com.yapp.web2.domain.account.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.exception.BusinessException
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.security.jwt.TokenDto
import com.yapp.web2.config.S3Uploader
import com.yapp.web2.exception.custom.AccountNotFoundException
import com.yapp.web2.exception.custom.ExistNameException
import com.yapp.web2.util.Message
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import javax.transaction.Transactional

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val jwtProvider: JwtProvider,
    private val s3Uploader: S3Uploader
) {

    companion object {
        val accountNotFoundException = AccountNotFoundException()
    }

    fun oauth2LoginUser(dto: Account.AccountLoginRequest): Account.AccountLoginSuccess {
        var account = Account.requestToAccount(dto)

        account = when (val savedAccount = accountRepository.findByEmail(account.email)) {
            null -> createUser(account)
            else -> updateUser(savedAccount, account)
        }
        val tokenDto = jwtProvider.createToken(account)

        return Account.AccountLoginSuccess(tokenDto, account)
    }

    @Transactional
    protected fun updateUser(savedAccount: Account, receivedAccount: Account): Account {
        savedAccount.image = receivedAccount.image
        savedAccount.name = receivedAccount.name
        return savedAccount
    }

    @Transactional
    fun createUser(account: Account): Account {
        return accountRepository.save(account)
    }

    fun reIssuedAccessToken(accessToken: String, refreshToken: String): TokenDto {
        return jwtProvider.reIssuedAccessToken(accessToken, refreshToken)
    }

    fun checkNickNameDuplication(nickNameDto:Account.NextNickName): String {
        return when(accountRepository.findAccountByName(nickNameDto.nickName)) {
            null -> Message.AVAILABLE_NAME
            else -> throw ExistNameException()
        }
    }

    @Transactional
    fun changeNickName(token: String, nextNickName: Account.NextNickName) {
        val idFromToken = jwtProvider.getIdFromToken(token)
        accountRepository.findById(idFromToken).let {
            if (it.isEmpty) throw accountNotFoundException
            it.get().name = nextNickName.nickName
            it
        }
    }

    @Transactional
    fun changeProfileImage(token: String, profile: MultipartFile): String {
        val idFromToken = jwtProvider.getIdFromToken(token)
        val account = accountRepository.findById(idFromToken).let {
            if (it.isEmpty) throw accountNotFoundException
            it.get().image = s3Uploader.upload(profile, "static")
            it
        }
        return account.get().image
    }

    @Transactional
    fun deleteProfileImage(token: String) {
        val idFromToken = jwtProvider.getIdFromToken(token)
        accountRepository.findById(idFromToken).let {
            if (it.isEmpty) throw accountNotFoundException
            if (it.get().image == Account.BASIC_IMAGE_URL) throw BusinessException("사진이 이미 존재하지 않습니다")
            it.get().image = Account.BASIC_IMAGE_URL
        }
    }

    @Transactional
    fun changeBackgroundColor(token: String, changeUrl: String) {
        val idFromToken = jwtProvider.getIdFromToken(token)
        accountRepository.findById(idFromToken).let {
            if (it.isEmpty) throw accountNotFoundException
            it.get().backgroundColor = changeUrl
        }
    }
}