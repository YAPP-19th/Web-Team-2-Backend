package com.yapp.web2.domain.account.service

import com.yapp.web2.common.PasswordValidator
import com.yapp.web2.config.S3Uploader
import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.entity.AccountRequestDto
import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.domain.folder.service.FolderService
import com.yapp.web2.exception.BusinessException
import com.yapp.web2.exception.custom.PasswordMismatchException
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.security.jwt.TokenDto
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.multipart.MultipartFile
import java.util.Optional
import kotlin.IllegalStateException

@ExtendWith(MockKExtension::class)
internal open class AccountServiceTest {

    @InjectMockKs
    private lateinit var accountService: AccountService

    @MockK
    private lateinit var folderService: FolderService

    @MockK
    private lateinit var accountRepository: AccountRepository

    @MockK
    private lateinit var jwtProvider: JwtProvider

    @MockK
    private lateinit var s3Uploader: S3Uploader

    @MockK
    private lateinit var passwordEncoder: PasswordEncoder

    @MockK
    private lateinit var mailSender: JavaMailSender

    private lateinit var testAccount: Account

    private lateinit var validator: PasswordValidator

    @BeforeEach
    internal fun init() {
        testAccount = Account("test@gmail.com", "1234567!")
        validator = PasswordValidator()
    }

    @Test
    fun `회원 가입할 이메일에서 닉네임을 가져온다`() {
        // given
        val email = "abc@gmail.com"

        // when
        val actual = accountService.getNickName(email)

        // then
        assertThat(actual).isEqualTo("abc")
    }

    @Test
    fun `회원가입에 성공한다`() {
        // given
        val testToken = TokenDto("testAccessToken", "testRefreshToken")
        val request = AccountRequestDto.SignUpRequest("abc@gmail.com", "12341234", "testFcmToken")
        val testAccount = Account.signUpToAccount(request, "2b86ff88ef6c4906482731gf15ddcb24381d34b", "abc")

        every { accountRepository.findByEmail(request.email) } returns null
        every { passwordEncoder.encode(request.password) } returns "2b86ff88ef6c4906482731gf15ddcb24381d34b"
        every { accountRepository.save(any()) } returns testAccount
        every { folderService.createDefaultFolder(any()) } just Runs
        every { jwtProvider.createToken(any()) } returns testToken

        // when
        val actual = accountService.signUp(request)

        // then
        assertAll(
            { assertThat(actual.accessToken).isEqualTo(testToken.accessToken) },
            { assertThat(actual.refreshToken).isEqualTo(testToken.refreshToken) },
            { assertThat(actual.email).isEqualTo(request.email) },
            { assertThat(actual.isRegistered).isFalse() },
            { assertThat(actual.name).isEqualTo("abc") },
        )
    }

    @Test
    fun `회원가입 시 기존 이메일이 존재하면 예외를 반환한다`() {
        // given
        val request = AccountRequestDto.SignUpRequest("abc@gmail.com", "12341234", "testFcmToken")
        every { accountRepository.findByEmail(any()) }.throws(IllegalStateException())

        // then
        org.junit.jupiter.api.assertThrows<IllegalStateException> { accountService.signUp(request) }
    }

    @Test
    fun `현재 비밀번호와 비교한다`() {
        // given
        val currentPassword = AccountRequestDto.CurrentPassword("1234567!")
        every { jwtProvider.getAccountFromToken(any()) } returns testAccount
        every { passwordEncoder.matches(any(), any()) } returns true

        // when
        accountService.comparePassword("testToken", currentPassword)

        // then
        verify(exactly = 1) { passwordEncoder.matches(any(), any()) }
    }

    @Test
    fun `비밀번호를 정상적으로 변경한다`() {
        // given
        val request = AccountRequestDto.PasswordChangeRequest("1234567!", "test")
        val expected = "비밀번호가 정상적으로 변경되었습니다."
        every { jwtProvider.getAccountFromToken(any()) } returns testAccount
        every { passwordEncoder.matches(any(), any()) } returns true
        every { passwordEncoder.encode(any()) } returns "test"

        // when
        val actual = accountService.changePassword("test", request)

        //then
        assertEquals(expected, actual)
    }

    @Test
    fun `현재 비밀번호와 다를경우 예외를 반환한다`() {
        // given
        val request = AccountRequestDto.PasswordChangeRequest("1234567!@", "test")
        val expectedMessage = "비밀번호가 일치하지 않습니다."
        every { jwtProvider.getAccountFromToken(any()) } returns testAccount
        every { passwordEncoder.matches(any(), any()) }.throws(PasswordMismatchException())

        // when
        val actualException = assertThrows(PasswordMismatchException::class.java) {
            accountService.changePassword("test", request)
        }

        //then
        assertEquals(expectedMessage, actualException.message)
    }

    @ParameterizedTest
    @ValueSource(strings = ["abcd1234", "1234abcd", "a1b2c3d4e5f6", "a123456789", "1abcdefg"])
    fun `비밀번호가 영문자와 숫자의 조합으로 8자에서 16자 사이의 길이일 경우 검증에 성공한다`(successPassword: String) {
        assertThat(validator.isValid(successPassword, null)).isTrue
    }

    @ParameterizedTest
    @ValueSource(strings = ["abcdefg!", "abcdefg-", "!@#$%^&a", "asdasd!@#", "a!b@c#de%f"])
    fun `비밀번호가 영문자와 특수문자의 조합으로 8자에서 16자 사이의 길이일 경우 검증에 성공한다`(successPassword: String) {
        assertThat(validator.isValid(successPassword, null)).isTrue
    }

    @ParameterizedTest
    @ValueSource(strings = ["1234567!", "1!2@3#4$5%", "1!@#$%^&", "12345678!@#$%^&"])
    fun `비밀번호가 숫자와 특수문자의 조합으로 8자에서 16자 사이의 길이일 경우 검증에 성공한다`(successPassword: String) {
        assertThat(validator.isValid(successPassword, null)).isTrue
    }

    @ParameterizedTest
    @ValueSource(strings = ["abc1234!@", "1!abcdefg", "a1!b2@c3#", "123Abcd!@#", "1!2@3#abcd"])
    fun `비밀번호가 영문자 숫자 특수문자 조합으로 8자에서 16자 사이의 길이일 경우 검증에 성공한다`(successPassword: String) {
        assertThat(validator.isValid(successPassword, null)).isTrue
    }

    @ParameterizedTest
    @ValueSource(strings = ["123456789", "abcdabcd", "!@#$!@#$!@"])
    fun `영문자 숫자 특수문자 중 2종류 이상의 조합이 아닌 비밀번호의 경우 검증에 실패한다`(failPassword: String) {
        assertThat(validator.isValid(failPassword, null)).isFalse
    }


    @ParameterizedTest
    @ValueSource(strings = ["", " ", "1234", "abcd", "1234567", "123456!", "0123456789abcdefgh"])
    fun `길이가 8자 미만 혹은 16자 초과하는 패스워드는 검증에 실패한다`(failPassword: String) {
        assertThat(validator.isValid(failPassword, null)).isFalse
    }

    @Test
    fun `회원을 정상적으로 탈퇴한다`() {
        // given
        every { jwtProvider.getAccountFromToken(any()) } returns testAccount

        // when
        accountService.softDelete("any")

        // then
        assertThat(testAccount.deleted).isTrue
    }

    @Test
    fun `비밀번호 설정 시 입력한 이메일이 존재하는지 확인한다`() {
        // given
        val request = AccountRequestDto.EmailCheckRequest("test@gmail.com")
        val expected = "입력하신 이메일 주소가 정상적으로 확인되었습니다."

        every { accountRepository.findByEmail(any()) } returns testAccount

        // when
        val actual = accountService.checkEmailExist("any", request)

        // then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `임시 비밀번호를 생성한다`() {
        // when
        val temp = accountService.createTempPassword()

        // then
        assertThat(temp.length).isEqualTo(13)
    }

    @Test
    fun `이메일이 존재할경우 true를 반환한다`() {
        // given
        val request = AccountRequestDto.SignUpEmailRequest("test@gmail.com")
        every { accountRepository.findByEmail(any()) } returns testAccount

        // when
        val actual = accountService.checkEmail(request)

        // then
        assertTrue(actual)
    }

    @Test
    fun `이메일이 존재하지 않을경우 false를 반환한다`() {
        // given
        val request = AccountRequestDto.SignUpEmailRequest("test@gmail.com")
        every { accountRepository.findByEmail(any()) } returns null

        // when
        val actual = accountService.checkEmail(request)

        // then
        assertFalse(actual)
    }

    @Test
    fun `FCM Token값을 정상적으로 설정한다`() {
        // given
        val expected = "test-token"
        val request = AccountRequestDto.FcmToken(expected)
        every { jwtProvider.getAccountFromToken(any()) } returns testAccount

        // when
        accountService.registerFcmToken("token", request)

        // then
        assertThat(testAccount.fcmToken).isEqualTo(expected)
    }

    @Nested
    inner class Profile {

        private lateinit var testToken: String
        private lateinit var testAccount: Account

        @BeforeEach
        internal fun setUp() {
            testToken = "testToken"
            testAccount = Account("test@naver.com", "imageUrl", "nickNameTest", "google", "testFcmToken")
        }

        @Test
        fun `account가 존재하지 않으면 예외를 던진다`() {
            // given
            // when
            // then
        }
    }

    @Nested
    inner class ChangeNickName {

        private lateinit var testToken: String
        private lateinit var testNickName: Account.NextNickName
        private lateinit var account: Account

        @BeforeEach
        internal fun setUp() {
            testToken = "testToken"
            testNickName = Account.NextNickName("testNickName")
            account = Account("test")
            account.image = "https://yapp-bucket-test.s3.ap-northeast-2.amazonaws.com/basicImage.png"
        }

        @Test
        fun `해당 닉네임을 다른 유저가 사용하고 있다면 예외를 던진다`() {
            //given
            every { accountRepository.findAccountByName(testNickName.nickName) } returns listOf(account)
            every { jwtProvider.getAccountFromToken(testToken) } returns account
            val expectedException = BusinessException("이미 존재하는 닉네임입니다")

            //when
            val actualException = assertThrows(BusinessException::class.java) {
                accountService.checkNickNameDuplication(testToken, testNickName)
            }

            //then
            assertEquals(expectedException.message, actualException.message)
        }

        @Test
        fun `Account가 존재하지 않으면 예외를 던진다`() {
            //given
            every { jwtProvider.getAccountFromToken(testToken) } returns account
            every { accountRepository.findById(any()) } returns Optional.empty()
            val expectedException = BusinessException("계정이 존재하지 않습니다.")

            //when
            val actualException = assertThrows(BusinessException::class.java) {
                accountService.changeNickName(testToken, testNickName)
            }

            //then
            assertEquals(expectedException.message, actualException.message)
        }

        @Test
        fun `닉네임이 변경된다`() {
            //given
            every { jwtProvider.getAccountFromToken(testToken) } returns account
            every { accountRepository.findById(any()) } returns Optional.of(account)

            //when
            accountService.changeNickName(testToken, testNickName)

            //then
            assertEquals(testNickName.nickName, account.name)
        }
    }

    @Nested
    inner class ProfileImageChange {
        private lateinit var testToken: String
        private lateinit var account: Account

        @MockK
        private lateinit var testFile: MultipartFile

        @BeforeEach
        internal fun setUp() {
            testToken = "testToken"
            account = Account("test")
            testFile = MockMultipartFile("file", "imagefile.jpeg", "image/jpeg", "<<jpeg data>>".encodeToByteArray())
        }

        @Test
        fun `Account가 존재하지 않으면 예외를 던진다`() {
            //given
            every { jwtProvider.getAccountFromToken(testToken) } returns account
            every { accountRepository.findById(any()) } returns Optional.empty()
            every { s3Uploader.upload(any(), any()) } returns "good/test"
            val expectedException = BusinessException("계정이 존재하지 않습니다.")

            //when
            val actualException = assertThrows(BusinessException::class.java) {
                accountService.changeProfileImage(testToken, testFile)
            }
            //then
            assertEquals(expectedException.message, actualException.message)
        }

        @Test
        fun `이미 사진이 존재하지 않을 때(== 기본 이미지 일 때,) 예외를 던진다`() {
            //given
            every { jwtProvider.getAccountFromToken(testToken) } returns account
            every { accountRepository.findById(1) } returns Optional.of(account)
            val expectedException = BusinessException("이미지가 존재하지 않습니다")

            //when
            val actualException = assertThrows(BusinessException::class.java) {
                accountService.deleteProfileImage(testToken)
            }

            //then
            assertEquals(expectedException.message, actualException.message)
        }

        @Test
        fun `이미지가 삭제 되고, 기본 이미지로 변경된다`() {
            //given
            every { jwtProvider.getAccountFromToken(testToken) } returns account
            every { accountRepository.findById(1) } returns Optional.of(account)
            account.image = "testImageURL"

            //when
            accountService.deleteProfileImage(testToken)

            //then
            assertEquals(account.image, Account.BASIC_IMAGE_URL)
        }
    }

    @Nested
    inner class BackgroundColorSetting {
        private lateinit var testToken: String
        private lateinit var request: AccountRequestDto.ChangeBackgroundColorRequest
        private lateinit var account: Account

        @BeforeEach
        internal fun setUp() {
            testToken = "testToken"
            request = AccountRequestDto.ChangeBackgroundColorRequest("http://yapp-bucket-test/test/image")
            account = Account("testAccount")
        }

        @Test
        fun `배경색이 변경된다`() {
            //given
            every { jwtProvider.getAccountFromToken(testToken) } returns account
            every { accountRepository.findById(1) } returns Optional.of(account)

            //when
            accountService.changeBackgroundColor(testToken, request)

            //then
            assertThat(account.backgroundColor).isEqualTo(request.changeUrl)
        }
    }
}