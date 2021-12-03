package com.yapp.web2.domain.account.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.exception.BusinessException
import com.yapp.web2.security.jwt.JwtProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

internal class AccountServiceTest {

    @MockK
    private lateinit var accountRepository: AccountRepository

    @MockK
    private lateinit var jwtProvider: JwtProvider
    private lateinit var accountService: AccountService

    @BeforeEach
    internal fun init() {
        MockKAnnotations.init(this)
        accountService = AccountService(accountRepository, jwtProvider)
    }



    @Nested
    inner class ChangeNickName {

        private lateinit var testToken: String
        private lateinit var testNickName: Account.nextNickName
        private lateinit var account: Account

        @BeforeEach
        internal fun setUp() {
            testToken = "testToken"
            testNickName = Account.nextNickName("testNickName")
            account = Account("test")
        }

        @Test
        fun `Account가 존재하지 않으면 예외를 던진다`() {
            //given
            every { jwtProvider.getIdFromToken(testToken) } returns 1
            every { accountRepository.findById(any()) } returns Optional.empty()
            val expectedException = BusinessException("계정이 존재하지 않습니다.")

            //when
            val actualException = Assertions.assertThrows(BusinessException::class.java) {
                accountService.changeNickName(testToken, testNickName)
            }
            //then
            assertEquals(expectedException.message, actualException.message)
        }

        //controller 쪽에서 검사하자
//        @Test
//        fun `닉네임의 길이가 넘어가면 예외를 던진다`() {
//
//        }
//
//        @Test
//        fun `변경 이름이 존재하지 않는다면 예외를 던진다`() {
//
//        }

        @Test
        fun `닉네임이 변경된다`() {
            //given
            every { jwtProvider.getIdFromToken(testToken) } returns 1
            every { accountRepository.findById(any()) } returns Optional.of(account)

            //when
            accountService.changeNickName(testToken, testNickName)

            //then
            assertEquals(testNickName.nickName, account.nickname)
        }
    }

    @Nested
    inner class ProfileImageChange {

    }

    @Nested
    inner class BackgroundColorSetting {

    }
}