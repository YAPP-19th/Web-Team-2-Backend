package com.yapp.web2.domain.account.service

import com.yapp.web2.config.S3Uploader
import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.domain.folder.entity.AccountFolder
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.service.FolderService
import com.yapp.web2.exception.BusinessException
import com.yapp.web2.exception.custom.AlreadyInvitedException
import com.yapp.web2.exception.custom.FolderNotRootException
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.util.AES256Util
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.util.*

@ExtendWith(MockKExtension::class)
internal class AccountServiceTest {

    @MockK
    private lateinit var accountRepository: AccountRepository

    @MockK
    private lateinit var folderService: FolderService

    @MockK
    private lateinit var jwtProvider: JwtProvider

    @MockK
    private lateinit var s3Uploader: S3Uploader

    @MockK
    private lateinit var aeS256Util: AES256Util

    @InjectMockKs
    private lateinit var accountService: AccountService

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

        @Test
        fun `보관함에 유저가 추가된다`() {
            // given
            val testFolder = Folder("test", 0, 0, null)
            val testFolderToken = "testFolderToken"

            every { jwtProvider.getAccountFromToken(any()) } returns testAccount
            every { aeS256Util.decrypt(testFolderToken) } returns "3"
            every { folderService.findByFolderId(any()) } returns testFolder

            // when
            accountService.acceptInvitation(testToken, testFolderToken)

            // then
            assertThat(testFolder.folders?.size).isEqualTo(1)
        }

        @Test
        fun `초대받은 링크가 보관함이 아닐 경우에 예외를 던진다`() {
            // given
            val testFolder = Folder("test", 0, 0, null)
            testFolder.rootFolderId = 1L
            val testFolderToken = "testFolderToken"

            every { jwtProvider.getAccountFromToken(any()) } returns testAccount
            every { aeS256Util.decrypt(testFolderToken) } returns "3"
            every { folderService.findByFolderId(any()) } returns testFolder

            // when + then
            assertThrows<FolderNotRootException> { accountService.acceptInvitation(testToken, testFolderToken) }
        }

        @Test
        fun `초대받은 링크를 이미 사용한 경우에는 예외를 던진다`() {
            // given
            val testFolder = Folder("test", 0, 0, null)
            val testFolderToken = "testFolderToken"
            testAccount.accountFolderList.add(AccountFolder(testAccount, testFolder))

            every { jwtProvider.getAccountFromToken(any()) } returns testAccount
            every { aeS256Util.decrypt(testFolderToken) } returns "3"
            every { folderService.findByFolderId(any()) } returns testFolder

            // when + then
            assertThrows<AlreadyInvitedException> { accountService.acceptInvitation(testToken, testFolderToken) }
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
            val actualException = assertThrows<BusinessException> {
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
            val actualException = assertThrows<BusinessException> {
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
            val actualException = assertThrows<BusinessException> {
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
            val actualException = assertThrows<BusinessException> {
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