package com.yapp.web2.domain.account.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.exception.BusinessException
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.security.jwt.TokenDto
import com.yapp.web2.config.S3Uploader
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import javax.transaction.Transactional

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val jwtProvider: JwtProvider,
    private val s3Uploader: S3Uploader
) {
    fun oauth2LoginUser(dto: Account.AccountLoginRequest): TokenDto {
        var account = Account.requestToAccount(dto)

        account = when (val savedAccount = accountRepository.findByEmail(account.email)) {
            null -> createUser(account)
            else -> updateUser(savedAccount, account)
        }

        return jwtProvider.createToken(account)
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

    fun checkNickNameDuplication(nickNameDto:String): String {
        val account = accountRepository.findByName(nickNameDto)
        return when (val account = accountRepository.findByName(nickNameDto)) {
            null -> "사용가능한 닉네임입니다."
            else -> "이미 사용중인 닉네임입니다."
        }
    }

    @Transactional
    fun changeNickName(token: String, nextNickName: String): Unit {
        val idFromToken = jwtProvider.getIdFromToken(token)
        val account = accountRepository.findById(idFromToken).let {
            if (it.isEmpty) throw BusinessException("계정이 존재하지 않습니다.")
            it.get().name = nextNickName
            it
        }
    }

    @Transactional
    fun changeProfileImage(token: String, profile: MultipartFile): String {
        val idFromToken = jwtProvider.getIdFromToken(token)
        val account = accountRepository.findById(idFromToken).let {
            if (it.isEmpty) throw BusinessException("계정이 존재하지 않습니다.")
            it.get().image = s3Uploader.upload(profile, "static")
            it
        }
        return account.get().image
    }

    @Transactional
    fun deleteProfileImage(token: String) {
        val idFromToken = jwtProvider.getIdFromToken(token)
        accountRepository.findById(idFromToken).let {
            if (it.isEmpty) throw BusinessException("계정이 존재하지 않습니다.")
            if (it.get().image == Account.BASIC_IMAGE_URL) throw BusinessException("사진이 이미 존재하지 않습니다")
            it.get().image = Account.BASIC_IMAGE_URL
        }
    }

    @Transactional
    fun changeBackgroundColor(token: String, changeUrl: String) {
        val idFromToken = jwtProvider.getIdFromToken(token)
        accountRepository.findById(idFromToken).let {
            if (it.isEmpty) throw BusinessException("계정이 존재하지 않습니다.")
            it.get().backgroundColor = changeUrl
        }
    }
}