package com.yapp.web2.domain.remind.service

import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.remind.entity.dto.ReadRemindListRequest
import com.yapp.web2.domain.remind.entity.dto.RemindCycleRequest
import com.yapp.web2.domain.remind.entity.dto.RemindListResponse
import com.yapp.web2.domain.remind.entity.dto.RemindListResponseWrapper
import com.yapp.web2.domain.remind.entity.dto.RemindToggleRequest
import com.yapp.web2.exception.custom.AccountNotFoundException
import com.yapp.web2.exception.custom.BookmarkNotFoundException
import com.yapp.web2.infra.fcm.FirebaseService
import com.yapp.web2.infra.slack.SlackService
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.util.Message
import com.yapp.web2.util.RemindCycleUtil
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class RemindService(
    private val bookmarkRepository: BookmarkRepository,
    private val accountRepository: AccountRepository,
    private val jwtProvider: JwtProvider,
    private val firebaseService: FirebaseService,
    private val slackApi: SlackService
) {

    companion object {
        private val bookmarkNotFoundException = BookmarkNotFoundException()
        private val log = LoggerFactory.getLogger(RemindService::class.java)
    }

    /**
     * 오늘자 기준으로 리마인드 발송 대상인 Bookmark List 조회
     */
    fun getRemindBookmark(): MutableList<Bookmark> {
        val today = LocalDate.now().toString()
        val remindBookmarkList: MutableList<Bookmark> = bookmarkRepository.findAllBookmarkByRemindTime(today)

        // 리마인드 발송 대상이 아닌 리마인드 제거(발송 시각이 오늘이 아니거나, 리마인드 발송이 이미 처리된 리마인드)
        remindBookmarkList.forEach { bookmark ->
            bookmark.remindList.removeIf { remind ->
                (today != remind.remindTime) || remind.remindStatus
            }
        }
        return remindBookmarkList
    }

    fun sendNotification(bookmark: Bookmark) {
        bookmark.remindList.forEach { remind ->
            var response = ""
            kotlin.runCatching {
                response = firebaseService.sendMessage(remind.fcmToken, Message.NOTIFICATION_MESSAGE, bookmark.title!!)
            }.onSuccess {
                log.info("리마인드 발송 성공 => [User id: ${remind.userId}], response => $response")
            }.onFailure {
                val message = """
                    <!channel> 리마인드 발송 실패 - [유저 id: ${remind.userId}, 북마크 id: ${bookmark.id} 북마크 제목: ${bookmark.title}]
                    ${it.cause} - ${it.message}
                    ${it.stackTrace[0]}
                    ${it.stackTrace[1]}
                """.trimIndent()

                log.info("리마인드 발송 실패 => [유저 id: ${remind.userId}, 북마크 id: ${bookmark.id}, 북마크 제목: ${bookmark.title}]")
                log.info(it.stackTraceToString())
                slackApi.sendSlackAlarm(message)
            }
        }
    }

    /**
     * 프로필에서 리마인드 알람 받기 On & Off 설정 : 유저가 Off 할 경우 모든 리마인드가 삭제된다는 alert 띄어주는게 좋을 듯 함
가     * On 일 경우: Account의 remindToggle 값만 true로 설정
     * Off 일 경우:
     *   1. 유저가 Off할 Case는 별로 없을 듯 하나 로그를 남겨 사용자 분석
     *   2. Bookmark의 remindList 중 userId와 동일한 리마인드 전부 삭제
     *   3. Account의 remindToggle 값만 false로 설정
     */
    // TODO: 상용 서버에서 해당 메서드를 처리하는 로직이 느린듯한데 파악해볼 것
    fun changeRemindToggle(request: RemindToggleRequest, accessToken: String) {
        val userId = jwtProvider.getIdFromToken(accessToken)

        // 1. logging
        log.info("$userId 번 회원이 리마인드 알람 받기 설정을 ${request.remindToggle}로 변경하였습니다.")

        if (isRemindOff(request.remindToggle)) {
            // 2. 해당 유저의 모든 Remind 삭제
            bookmarkRepository.findAllBookmarkByUserIdInRemindList(userId).forEach { bookmark ->
                bookmark.remindList.removeIf { remind ->
                    userId == remind.userId
                }
                bookmarkRepository.save(bookmark)
            }
        }

        // 3. remindToggle 값 변경(true or false)
        accountRepository.findAccountById(userId)?.let {
            it.inverseRemindToggle(request.remindToggle)
            accountRepository.save(it)
        } ?: run {
            log.info("$userId 에 해당하는 회원을 찾을 수 없습니다.")
            throw AccountNotFoundException()
        }
    }

    private fun isRemindOff(remindToggle: Boolean) = !remindToggle

    /**
     * 프로필 -> 리마인드 주기 설정
     */
    fun updateRemindAlarmCycle(request: RemindCycleRequest, accessToken: String) {
        RemindCycleUtil.validRemindCycle(request.remindCycle)

        val userId = jwtProvider.getIdFromToken(accessToken)
        accountRepository.findByIdOrNull(userId)?.let {
            it.remindCycle = request.remindCycle
            accountRepository.save(it)
        }
    }

    // TODO: bookmarkService.toggleOffRemindBookmark 메서드랑 동일한 로직인 듯 함, 프론트에서 어떠한 URI 사용하는지 확인필요
    fun bookmarkRemindOff(accessToken: String, bookmarkId: String) {
        val accountId = jwtProvider.getIdFromToken(accessToken)

        bookmarkRepository.findByIdOrNull(bookmarkId)?.let {
            it.remindOff(accountId)
            bookmarkRepository.save(it)
        } ?: run {
            log.error("Remind off failed. Bookmark not exist => userId: ${accountId}, bookmarkId: $bookmarkId")
            throw BookmarkNotFoundException()
        }

    }

    /**
     * 도토리함 메인 화면에서 리마인드가 발송된 북마크 리스트 조회
     */
    fun getRemindList(accessToken: String): RemindListResponseWrapper {
        val userId = jwtProvider.getIdFromToken(accessToken)
        val bookmarks: List<Bookmark> = bookmarkRepository.findAllBookmarkByUserIdAndRemindsInRemindList(userId)
        val responseWrapper = RemindListResponseWrapper()
        val contents = responseWrapper.contents

        bookmarks.forEach { bookmark ->
            // bookmark의 remindList에서 userId는 중복되는 케이스가 없으므로 리마인드가 존재하면 반드시 1건만 존재
            val remind = bookmark.remindList[0]
            val date = LocalDate.parse(remind.remindTime, DateTimeFormatter.ISO_DATE)
            val pushTime = date.atTime(13, 0, 0)
            contents.add(RemindListResponse(bookmark.id, bookmark.title!!, pushTime))
        }
        return responseWrapper
    }

    /**
     * 발송된 리마인드 중 사용자가 읽음 처리한 리마인드의 BookmarkId List
     */
    fun remindCheckUpdate(accessToken: String, request: ReadRemindListRequest) {
        val userId = jwtProvider.getIdFromToken(accessToken)

        // 1) bookmarkId에 해당하는 북마크 조회
        request.bookmarkIdList.stream().forEach { id ->
            bookmarkRepository.findByIdOrNull(id)?.let { bookmark ->
                // 2) bookmark 필드인 remindList 리스트에서 userId 동일한 remind 검색 후 update
                bookmark.remindList.forEach { remind ->
                    if (remind.userId == userId) {
                        remind.updateRemindCheck()
                        bookmarkRepository.save(bookmark)
                    }
                }
            } ?: run {
                log.info("Bookmark not exist => bookmarkId: $id")
                throw bookmarkNotFoundException
            }
        }
    }

    fun save(entity: Bookmark) {
        bookmarkRepository.save(entity)
    }

}