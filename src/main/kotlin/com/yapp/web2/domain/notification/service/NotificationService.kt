package com.yapp.web2.domain.notification.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.UserRepository
import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class NotificationService(
    private val bookmarkRepository: BookmarkRepository,
    private val userRepository: UserRepository
) {
    fun getRemindBookmark(): List<Bookmark> {
        val now = LocalDate.now()
        return bookmarkRepository.findAllByRemindTimeAndDeleteTimeIsNull(now)
    }

    // notificaiton 부분 추가
    fun sendNotification(userId: Long) {
        // userId에 해당하는 account를 들고와서 전송시켜야한다.
    }
}