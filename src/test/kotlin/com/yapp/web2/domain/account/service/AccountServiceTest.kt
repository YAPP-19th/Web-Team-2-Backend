package com.yapp.web2.domain.account.service

import com.yapp.web2.config.S3Uploader
import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.entity.AccountRequestDto
import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.domain.folder.service.FolderService
import com.yapp.web2.exception.BusinessException
import com.yapp.web2.exception.custom.PasswordMismatchException
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.security.jwt.TokenDto
import com.yapp.web2.util.Message
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.multipart.MultipartFile
import java.util.*
import javax.validation.Validation
import kotlin.IllegalStateException

internal class AccountServiceTest {

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

    private lateinit var testAccount: Account

    @BeforeEach
    internal fun init() {
        MockKAnnotations.init(this)
        accountService = AccountService(folderService, accountRepository, jwtProvider, s3Uploader, passwordEncoder)
        testAccount = Account("test@gmail.com", "1234567!")
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
        val actual = accountService.singUp(request)

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
        org.junit.jupiter.api.assertThrows<IllegalStateException> { accountService.singUp(request) }
    }

    @Test
    fun `회원가입 시 이메일 형식을 검사한다`() {
        // TODO: 2022/05/05
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

    @Test
    fun `비밀번호의 길이는 8자에서 16자 사이여야 한다`() {
        // given
        val lessThanEightLength = AccountRequestDto.PasswordChangeRequest("123456!", "123456!")
        val moreThenSixteenLength = AccountRequestDto.PasswordChangeRequest("123456789abcdefg!@", "123456789abcdefg!@")
        val validatorFactory = Validation.buildDefaultValidatorFactory()
        val validator = validatorFactory.validator

        // when
        val constraints = validator.validate(lessThanEightLength)
        val constraints2 = validator.validate(moreThenSixteenLength)

        assertAll(
            { assertThat(constraints.stream().filter { it.messageTemplate.equals(Message.PASSWORD_VALID_MESSAGE) }) },
            { assertThat(constraints2.stream().filter { it.messageTemplate.equals(Message.PASSWORD_VALID_MESSAGE) }) }
        )
    }

    @Test
    fun `비밀번호는 특수문자를 반드시 포함해야 한다`() {
        // given
        val notContainsSpecialCharacters = AccountRequestDto.PasswordChangeRequest("12345678", "12345678")
        val validatorFactory = Validation.buildDefaultValidatorFactory()
        val validator = validatorFactory.validator

        // when
        val constraints = validator.validate(notContainsSpecialCharacters)

        // then
        assertThat(constraints.stream().filter { it.messageTemplate.equals(Message.PASSWORD_VALID_MESSAGE) })
    }

    @Test
    fun `비밀번호는 특수문자를 포함하여 영문자 혹은 숫자를 포함해야 한다`() {
        // TODO: 2022/05/05
        val rightPassword1 = AccountRequestDto.PasswordChangeRequest("1234567!", "1234567!")
        val rightPassword2 = AccountRequestDto.PasswordChangeRequest("abcdefg!", "abcdefg!")
        val validatorFactory = Validation.buildDefaultValidatorFactory()
        val validator = validatorFactory.validator

        // when
        val constraints1 = validator.validate(rightPassword1)
        val constraints2 = validator.validate(rightPassword2)

        // then
        assertAll(
            { assertThat(constraints1).isEmpty() },
            { assertThat(constraints2).isEmpty() }
        )
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
        private lateinit var testBackgroundColorUrl: String
        private lateinit var account: Account

        @BeforeEach
        internal fun setUp() {
            testToken = "testToken"
            testBackgroundColorUrl = "http://yapp-bucket-test/test/image"
            account = Account("testAccount")
        }

        @Test
        fun `배경색이 변경된다`() {
            //given
            every { jwtProvider.getAccountFromToken(testToken) } returns account
            every { accountRepository.findById(1) } returns Optional.of(account)

            //when
            accountService.changeBackgroundColor(testToken, testBackgroundColorUrl)

            //then
            assertThat(account.backgroundColor).isEqualTo(testBackgroundColorUrl)
        }
    }
}