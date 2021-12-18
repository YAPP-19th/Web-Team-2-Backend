package com.yapp.web2.domain.remind.service

import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.remind.entity.dto.RemindCycleRequest
import com.yapp.web2.domain.remind.entity.dto.RemindToggleRequest
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.util.RemindCycleUtil
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class RemindService(
    private val bookmarkRepository: BookmarkRepository,
    private val accountRepository: AccountRepository,
    private val jwtProvider: JwtProvider
) {
    fun getRemindBookmark(): List<Bookmark> {
        val now = LocalDate.now()
        return bookmarkRepository.findAllByRemindTimeAndDeleteTimeIsNull(now)
    }

    // notificaiton 부분 추가
    fun sendNotification(userId: Long) {
        // userId에 해당하는 account를 들고와서 전송시켜야한다.
    }

    @Transactional
    fun changeRemindToggle(request: RemindToggleRequest, accessToken: String) {
        val userId = jwtProvider.getIdFromToken(accessToken)

        // 리마인드 알림을 Off 할 때, 모든 리마인드 주기 Null 처리
        if (isRemindOff(request.remindToggle)) {
            bookmarkRepository.findAllByUserId(userId).let {
                it.stream().forEach { bookmark ->
                    bookmark.remindOff()
                    bookmarkRepository.save(bookmark)
                }
            }
        }

        accountRepository.findByIdOrNull(userId)?.let {
            it.remindToggle = request.remindToggle
        }
    }

    private fun isRemindOff(remindToggle: Boolean) = !remindToggle

    @Transactional
    fun updateRemindAlarmCycle(request: RemindCycleRequest, accessToken: String) {
        RemindCycleUtil.validRemindCycle(request.remindCycle)

        val userId = jwtProvider.getIdFromToken(accessToken)
        accountRepository.findByIdOrNull(userId)?.let {
            it.remindCycle = request.remindCycle
        }

    }

    @Transactional
    fun bookmarkRemindOff(bookmarkId: String) {
        bookmarkRepository.findByIdOrNull(bookmarkId)?.let {
            it.remindOff()
            bookmarkRepository.save(it)
        }
    }
}