package com.yapp.web2.domain.remind.service

import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.remind.entity.dto.*
import com.yapp.web2.exception.custom.BookmarkNotFoundException
import com.yapp.web2.infra.fcm.FirebaseService
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.util.Message
import com.yapp.web2.util.RemindCycleUtil
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class RemindService(
    private val bookmarkRepository: BookmarkRepository,
    private val accountRepository: AccountRepository,
    private val jwtProvider: JwtProvider,
    private val firebaseService: FirebaseService
) {

    companion object {
        private val bookmarkNotFoundException = BookmarkNotFoundException()
    }

    fun getRemindBookmark(): List<Bookmark> {
        val today = LocalDate.now().toString()
        return bookmarkRepository.findAllByRemindTimeAndDeleteTimeIsNullAndRemindStatusIsFalse(today)
    }

    fun sendNotification(bookmark: Bookmark) {
        val user = accountRepository.findAccountById(bookmark.userId)
        val fcmToken = user?.fcmToken ?: throw IllegalStateException("${user!!.name} 님은 FCM Token을 가지고 있지 않습니다.")

        firebaseService.sendMessage(fcmToken, Message.NOTIFICATION_MESSAGE, bookmark.title!!)
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

    fun getRemindList(accessToken: String): RemindListResponseWrapper {
        val userId = jwtProvider.getIdFromToken(accessToken)
        val responseWrapper = RemindListResponseWrapper()
        val remindList = responseWrapper.contents
        val bookmarks = bookmarkRepository.findAllByUserIdAndRemindCheckIsFalseAndRemindStatusIsTrue(userId)

        bookmarks.stream()
            .forEach { bookmark ->
                val date = LocalDate.parse(bookmark.remindTime, DateTimeFormatter.ISO_DATE) // yyyy-MM-dd
                val pushTime = date.atTime(13, 0, 0)
                val remindResponse = RemindListResponse(bookmark.id, bookmark.title!!, pushTime!!)
                remindList.add(remindResponse)
            }
        return responseWrapper
    }

    fun remindCheckUpdate(request: ReadRemindListRequest) {
        request.bookmarkIdList.stream()
            .forEach { id ->
                bookmarkRepository.findByIdOrNull(id)?.let { bookmark ->
                    bookmark.updateRemindCheck()
                    bookmarkRepository.save(bookmark)
                } ?: bookmarkNotFoundException
            }
    }

    fun save(entity: Bookmark) {
        bookmarkRepository.save(entity)
    }
}