package com.yapp.web2.domain.account.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.security.jwt.TokenDto
import com.yapp.web2.config.S3Uploader
import com.yapp.web2.domain.account.entity.AccountRequestDto
import com.yapp.web2.domain.folder.service.FolderService
import com.yapp.web2.exception.BusinessException
import com.yapp.web2.exception.custom.EmailNotFoundException
import com.yapp.web2.exception.custom.ExistNameException
import com.yapp.web2.exception.custom.ImageNotFoundException
import com.yapp.web2.exception.custom.PasswordMismatchException
import com.yapp.web2.util.Message
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.lang.IllegalStateException
import javax.transaction.Transactional

@Service
@Transactional
class AccountService(
    private val folderService: FolderService,
    private val accountRepository: AccountRepository,
    private val jwtProvider: JwtProvider,
    private val s3Uploader: S3Uploader,
    private val passwordEncoder: PasswordEncoder,
    private val mailSender: JavaMailSender
) {

    @Value("\${extension.version}")
    private lateinit var extensionVersion: String

    @Value("\${spring.mail.username}")
    private lateinit var fromSender: String

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
        val account = Account.profileToAccount(dto)
        val existAccount = accountRepository.findByEmail(account.email)
        return signUpOrLogin(account, existAccount)
    }

    private fun signUpOrLogin(
        account: Account,
        existAccount: Account?
    ): Account.AccountLoginSuccess {
        var isRegistered = true
        var account2 = account

        account2 = when (existAccount) {
            null -> {
                isRegistered = false
                val newAccount = createUser(account)
                folderService.createDefaultFolder(account)
                newAccount
            }
            else -> {
                existAccount.fcmToken = account2.fcmToken
                createUser(existAccount)
            }
        }
        return Account.AccountLoginSuccess(jwtProvider.createToken(account2), account2, isRegistered)
    }

    fun singUp(dto: AccountRequestDto.SignUpRequest): Account.AccountLoginSuccess {
        if (accountRepository.findByEmail(dto.email) != null) {
            throw IllegalStateException(Message.EXIST_USER)
        }

        val encryptPassword = passwordEncoder.encode(dto.password)
        val nickName = getNickName(dto.email)
        val newAccount = createUser(Account.signUpToAccount(dto, encryptPassword, nickName))
        folderService.createDefaultFolder(newAccount)

        return Account.AccountLoginSuccess(jwtProvider.createToken(newAccount), newAccount, false)
    }

    internal fun getNickName(email: String): String {
        val atIndex = email.indexOf("@")
        return email.substring(0, atIndex)
    }

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

    fun changeNickName(token: String, nextNickName: Account.NextNickName) {
        val account = jwtProvider.getAccountFromToken(token)

        account.let {
            it.name = nextNickName.nickName
            it
        }
    }

    fun changeProfile(token: String, profileChanged: Account.ProfileChanged) {
        val account = jwtProvider.getAccountFromToken(token)
        account.let {
            it.image = profileChanged.profileImageUrl
            it.name = profileChanged.name
            it
        }
    }

    fun changeProfileImage(token: String, profile: MultipartFile): String {
        val account = jwtProvider.getAccountFromToken(token).let {
            it.image = s3Uploader.upload(profile, DIR_NAME)
            it
        }
        return account.image
    }

    fun deleteProfileImage(token: String) {
        val account = jwtProvider.getAccountFromToken(token)
        account.let {
            if (it.image == Account.BASIC_IMAGE_URL) throw ImageNotFoundException()
            it.image = Account.BASIC_IMAGE_URL
        }
    }

    fun changeBackgroundColor(token: String, changeUrl: String) {
        val account = jwtProvider.getAccountFromToken(token)
        account.backgroundColor = changeUrl
    }

    fun checkExtension(userVersion: String): String {
        return if (userVersion == extensionVersion) Message.LATEST_EXTENSION_VERSION
        else Message.OLD_EXTENSION_VERSION
    }

    fun signIn(request: AccountRequestDto.SignInRequest): Account.AccountLoginSuccess? {
        val account = accountRepository.findByEmail(request.email) ?: throw IllegalStateException(Message.NOT_EXIST_EMAIL)

        if (!passwordEncoder.matches(request.password, account.password)) {
            throw IllegalStateException(Message.USER_PASSWORD_MISMATCH)
        }

        return Account.AccountLoginSuccess(jwtProvider.createToken(account), account, false)
    }

    fun comparePassword(token: String, dto: AccountRequestDto.CurrentPassword): String {
        val account = jwtProvider.getAccountFromToken(token)
        if (!passwordEncoder.matches(dto.currentPassword, account.password)) {
            throw PasswordMismatchException()
        }
        return Message.SAME_PASSWORD
    }

    fun changePassword(token: String, dto: AccountRequestDto.PasswordChangeRequest): String {
        val account = jwtProvider.getAccountFromToken(token)
        if (!passwordEncoder.matches(dto.currentPassword, account.password)) {
            throw PasswordMismatchException()
        }
        account.password = passwordEncoder.encode(dto.newPassword)
        return Message.CHANGE_PASSWORD_SUCCEED
    }

    fun softDelete(token: String) {
        val account = jwtProvider.getAccountFromToken(token)
        account.softDeleteAccount()
    }

    fun checkEmailExist(token: String, request: AccountRequestDto.EmailCheckRequest): String {
        accountRepository.findByEmail(request.email)?.let {
            if (it.email != request.email) {
                throw EmailNotFoundException()
            }
        }
        return Message.SUCCESS_EXIST_EMAIL
    }

    internal fun createTempPassword(): String {
        return RandomStringUtils.randomAlphanumeric(12) + "!"
    }

    fun sendMail(token: String, tempPassword: String): String {
        val account = jwtProvider.getAccountFromToken(token)
        val mailMessage = SimpleMailMessage()
        mailMessage.setTo(account.email)
        mailMessage.setFrom(fromSender)
        mailMessage.setSubject("${account.name} 님의 임시비밀번호 안내 메일입니다.")
        mailMessage.setText("안녕하세요. \n\n 임시 비밀번호를 전달드립니다. \n\n 임시 비밀번호는: $tempPassword 입니다.")
        mailSender.send(mailMessage)

        return Message.SUCCESS_SEND_MAIL
    }

    fun updatePassword(token: String, tempPassword: String) {
        val account = jwtProvider.getAccountFromToken(token)
        account.password = passwordEncoder.encode(tempPassword)
    }

}