package com.yapp.web2.domain.notification.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.notification.entity.dto.RemindCycleRequest
import com.yapp.web2.domain.notification.entity.dto.RemindToggleRequest
import com.yapp.web2.security.jwt.JwtProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class NotificationService(
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
    fun changeRemindAlarm(request: RemindToggleRequest, accessToken: String) {
        val userId = jwtProvider.getIdFromToken(accessToken)

        accountRepository.findById(userId).ifPresent {
            it.remindToggle = request.remindToggle
            accountRepository.save(it)
        }
    }

    @Transactional
    fun updateRemindAlarmCycle(request: RemindCycleRequest, accessToken: String) {
        val userId = jwtProvider.getIdFromToken(accessToken)

        accountRepository.findById(userId).ifPresent {
            it.remindCycle = request.remindCycle
            accountRepository.save(it)
        }

    }
}