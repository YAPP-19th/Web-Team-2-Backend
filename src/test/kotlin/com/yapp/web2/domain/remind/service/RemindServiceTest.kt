package com.yapp.web2.domain.remind.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.remind.entity.dto.RemindCycleRequest
import com.yapp.web2.domain.remind.entity.dto.RemindToggleRequest
import com.yapp.web2.exception.custom.RemindCycleValidException
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.util.RemindCycleUtil
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull

@ExtendWith(MockKExtension::class)
internal class RemindServiceTest {

    @InjectMockKs
    private lateinit var remindService: RemindService

    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var accountRepository: AccountRepository

    @MockK
    private lateinit var jwtProvider: JwtProvider

    private lateinit var account: Account

    @BeforeEach
    fun setup() {
        account = Account("test@gmail.com")
    }

    @Test
    fun `리마인드 알람 설정을 OFF 한다`() {
        // given
        val request = RemindToggleRequest(false)

        // mock
        every { accountRepository.findByIdOrNull(any()) } returns account
        every { jwtProvider.getIdFromToken(any()) } returns 1L

        // when
        remindService.changeRemindToggle(request, "token")

        // then
        assertThat(account.remindToggle).isEqualTo(false)
    }

    @Test
    fun `리마인드 주기를 변경한다`() {
        // given
        val request1 = RemindCycleRequest(RemindCycleUtil.THIRTY_DAYS.days)

        // mock
        every { accountRepository.findByIdOrNull(any()) } returns account
        every { jwtProvider.getIdFromToken(any()) } returns 1L

        // when
        remindService.updateRemindAlarmCycle(request1, "token")

        // then
        assertThat(account.remindCycle).isEqualTo(30)
    }

    @Test
    fun `리마인드 주기가 3, 7, 14, 30일이 아니면 에러를 반환한다`() {
        // given
        val request1 = RemindCycleRequest(4)
        val request2 = RemindCycleRequest(11)

        // mock
        every { accountRepository.findByIdOrNull(any()) } returns account
        every { jwtProvider.getIdFromToken(any()) } returns 1L

        // when & then
        assertAll(
            { assertThrows<RemindCycleValidException> { remindService.updateRemindAlarmCycle(request1, "token") } },
            { assertThrows<RemindCycleValidException> { remindService.updateRemindAlarmCycle(request2, "token") } }
        )
    }
}