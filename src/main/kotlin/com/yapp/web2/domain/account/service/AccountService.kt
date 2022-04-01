package com.yapp.web2.domain.account.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.security.jwt.TokenDto
import com.yapp.web2.config.S3Uploader
import com.yapp.web2.domain.folder.service.FolderService
import com.yapp.web2.exception.BusinessException
import com.yapp.web2.exception.custom.ExistNameException
import com.yapp.web2.exception.custom.ImageNotFoundException
import com.yapp.web2.util.Message
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import javax.transaction.Transactional

@Service
class AccountService(
    private val folderService: FolderService,
    private val accountRepository: AccountRepository,
    private val jwtProvider: JwtProvider,
    private val s3Uploader: S3Uploader
) {

    @Value("\${extension.version}")
    private lateinit var extensionVersion: String

    companion object {
        private const val DIR_NAME = "static"
    }

    fun getRemindElements(token: String): Account.RemindElements {
        val account = jwtProvider.getAccountFromToken(token)
        return Account.accountToRemindElements(account)
    }

    fun getProfile(token: String): Account.AccountProfile {
        val account = jwtProvider.getAccountFromToken(token)
        return Account.accountToProfile(account)
    }

    fun oauth2LoginUser(dto: Account.AccountProfile): Account.AccountLoginSuccess {
        var account = Account.profileToAccount(dto)
        var isRegistered = true

        account = when (val savedAccount = accountRepository.findByEmail(account.email)) {
            null -> {
                isRegistered = false
                val account2 = createUser(account)
                folderService.createDefaultFolder(account)
                account2
            }
            else -> {
                savedAccount.fcmToken = account.fcmToken
                createUser(savedAccount)
            }
        }
        val tokenDto = jwtProvider.createToken(account)

        return Account.AccountLoginSuccess(tokenDto, account, isRegistered)
    }

    @Transactional
    fun createUser(account: Account): Account {
        return accountRepository.save(account)
    }

    fun reIssuedAccessToken(accessToken: String, refreshToken: String): TokenDto {
        return jwtProvider.reIssuedAccessToken(accessToken, refreshToken)
    }

    fun checkNickNameDuplication(token: String, nickNameDto: Account.NextNickName): String {
        val account = jwtProvider.getAccountFromToken(token)
        if (nickNameDto.nickName.isEmpty()) throw BusinessException("닉네임을 입력해주세요!")
        if (account.name == nickNameDto.nickName) return "본인의 닉네임입니다!"
        return when (accountRepository.findAccountByName(nickNameDto.nickName).isEmpty()) {
            true -> Message.AVAILABLE_NAME
            false -> throw ExistNameException()
        }
    }

    @Transactional
    fun changeNickName(token: String, nextNickName: Account.NextNickName) {
        val account = jwtProvider.getAccountFromToken(token)

        account.let {
            it.name = nextNickName.nickName
            it
        }
    }

    @Transactional
    fun changeProfile(token: String, profileChanged: Account.ProfileChanged) {
        val account = jwtProvider.getAccountFromToken(token)
        account.let {
            it.image = profileChanged.profileImageUrl
            it.name = profileChanged.name
            it
        }
    }

    @Transactional
    fun changeProfileImage(token: String, profile: MultipartFile): String {
        val account = jwtProvider.getAccountFromToken(token).let {
            it.image = s3Uploader.upload(profile, DIR_NAME)
            it
        }
        return account.image
    }

    @Transactional
    fun deleteProfileImage(token: String) {
        val account = jwtProvider.getAccountFromToken(token)
        account.let {
            if (it.image == Account.BASIC_IMAGE_URL) throw ImageNotFoundException()
            it.image = Account.BASIC_IMAGE_URL
        }
    }

    @Transactional
    fun changeBackgroundColor(token: String, changeUrl: String) {
        val account = jwtProvider.getAccountFromToken(token)
        account.backgroundColor = changeUrl
    }

    fun checkExtension(userVersion: String): String {
        return if (userVersion == extensionVersion) Message.LATEST_EXTENSION_VERSION
        else Message.OLD_EXTENSION_VERSION
    }
}