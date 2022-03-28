package com.yapp.web2.domain.remind.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.remind.entity.dto.ReadRemindListRequest
import com.yapp.web2.domain.remind.entity.dto.RemindCycleRequest
import com.yapp.web2.domain.remind.entity.dto.RemindToggleRequest
import com.yapp.web2.exception.custom.RemindCycleValidException
import com.yapp.web2.infra.fcm.FirebaseService
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.util.RemindCycleUtil
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
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

    @MockK
    private lateinit var firebaseService: FirebaseService

    private lateinit var account: Account
    private lateinit var bookmark1: Bookmark
    private lateinit var bookmark2: Bookmark
    private lateinit var bookmarkList: List<Bookmark>

    @BeforeEach
    fun setup() {
        account = Account("test@gmail.com")
        bookmark1 = Bookmark(1L, 2L, "https://www.naver.com")
        bookmark2 = Bookmark(1L, 2L, "https://www.google.com")
        bookmarkList = listOf(bookmark1, bookmark2)
    }

    @Test
    fun `리마인드 알람 설정을 OFF 한다`() {
        // given
        val request = RemindToggleRequest(false)

        // mock
        every { bookmarkRepository.findAllByUserId(any()) } returns bookmarkList
        every { bookmarkRepository.save(any()) } returns bookmark1
        every { accountRepository.findByIdOrNull(any()) } returns account
        every { jwtProvider.getIdFromToken(any()) } returns 1L

        // when
        remindService.changeRemindToggle(request, "token")

        // then
        assertAll(
            { assertThat(account.remindToggle).isEqualTo(false) },
            { bookmarkList.stream().forEach {
                assertThat(it.remindTime).isNull()
            } }
        )
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

    @Test
    fun `웹푸쉬 발송 후 Client가 확인하지 않은 리마인드 리스트를 조회한다`() {

    }

    @Test
    fun `Client가 리마인드를 확인했다는 처리를 진행한다`() {
        // given
        val bookmarkIdList: MutableList<String> = mutableListOf("61bdbbaa72b0f85372ad57c8", "16cdaa73j0f23785ad57c9")
        val request = ReadRemindListRequest(bookmarkIdList)

        // mock
        every { bookmarkRepository.findByIdOrNull("61bdbbaa72b0f85372ad57c8") } returns bookmark1
        every { bookmarkRepository.findByIdOrNull("16cdaa73j0f23785ad57c9") } returns bookmark2
        every { bookmarkRepository.save(any()) } returns bookmark1

        // when
        remindService.remindCheckUpdate(request)

        // then
        assertAll(
            { verify(exactly = bookmarkIdList.size) { bookmarkRepository.save(any()) } },
            { assertThat(bookmark1.remindCheck).isTrue() },
            { assertThat(bookmark2.remindCheck).isTrue() }
        )

    }
}