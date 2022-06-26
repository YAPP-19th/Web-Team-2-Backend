package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.util.AES256Util
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class BookmarkPageService(
    private val bookmarkRepository: BookmarkRepository,
    private val jwtProvider: JwtProvider,
    private val aes256Util: AES256Util
) {
    @Transactional(readOnly = true)
    fun getAllPageByFolderId(folderId: Long, pageable: Pageable, remind: Boolean): Page<Bookmark> {
        return when (remind) {
            true -> bookmarkRepository.findAllByFolderIdAndDeleteTimeIsNullAndRemindTimeIsNotNull(folderId, pageable)
            false -> bookmarkRepository.findAllByFolderIdAndDeleteTimeIsNull(folderId, pageable)
        }
    }

    fun getTrashPageByUserId(token: String, pageable: Pageable, remind: Boolean): Page<Bookmark> {
        val idFromToken = jwtProvider.getIdFromToken(token)
        return when (remind) {
            true -> bookmarkRepository.findAllByUserIdAndDeleteTimeIsNotNullAndRemindTimeIsNotNull(
                idFromToken,
                pageable
            )
            false -> bookmarkRepository.findAllByUserIdAndDeleteTimeIsNotNull(idFromToken, pageable)
        }
    }

    fun getAllPageByUserId(token: String, pageable: Pageable, remind: Boolean): Page<Bookmark> {
        val idFromToken = jwtProvider.getIdFromToken(token)

        return when (remind) {
            true -> bookmarkRepository.findAllByUserIdAndRemindTimeIsNotNullAndDeleteTimeIsNull(idFromToken, pageable)
            false -> bookmarkRepository.findAllByUserIdAndDeleteTimeIsNull(idFromToken, pageable)
        }
    }

    fun getTodayRemindBookmark(token: String): Bookmark.RemindList {
        val idFromToken = jwtProvider.getIdFromToken(token)
        val yesterday = LocalDate.now().minusDays(1).toString()

        return Bookmark.RemindList(
            bookmarkRepository.findAllByRemindTimeAfterAndUserIdAndDeleteTimeIsNull(
                yesterday,
                idFromToken
            )
        )
    }

    fun getAllPageByEncryptFolderId(token: String, pageable: Pageable): Page<Bookmark> {
        val folderIdByString = aes256Util.decrypt(token)
        return bookmarkRepository.findAllByFolderIdAndDeleteTimeIsNull(folderIdByString.toLong(), pageable)
    }
}