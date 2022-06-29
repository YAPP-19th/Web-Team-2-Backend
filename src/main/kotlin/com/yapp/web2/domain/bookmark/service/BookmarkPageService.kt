package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.BookmarkDto
import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.entity.BookmarkInterface
import com.yapp.web2.domain.bookmark.repository.BookmarkInterfaceRepository
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.util.AES256Util
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class BookmarkPageService(
    private val bookmarkRepository: BookmarkRepository,
    private val bookmarkInterfaceRepository: BookmarkInterfaceRepository,
    private val jwtProvider: JwtProvider,
    private val aes256Util: AES256Util
) {
    @Transactional(readOnly = true)
    fun getAllPageByFolderId(
        folderId: Long,
        pageable: Pageable,
        remind: Boolean
    ): Page<BookmarkDto.BookmarkRequestDto> {
        val list = when (remind) {
            true -> bookmarkRepository.findAllByFolderIdAndDeleteTimeIsNullAndRemindTimeIsNotNull(folderId, pageable)
            false -> bookmarkRepository.findAllByFolderIdAndDeleteTimeIsNull(folderId, pageable)
        }

        val bookmarkList = bookmarkToBookmarkRequestDto(list)
        val bookmarkInterfaceList =
            bookmarkInterfaceToBookmarkRequestDto(bookmarkInterfaceRepository.findAllByFolderId(folderId))

        val result = bookmarkList + bookmarkInterfaceList
        return PageImpl(result, pageable, result.size.toLong())
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

    fun getAllPageByUserId(token: String, pageable: Pageable, remind: Boolean): Page<BookmarkDto.BookmarkRequestDto> {
        val idFromToken = jwtProvider.getIdFromToken(token)

        val list = when (remind) {
            true -> bookmarkRepository.findAllByUserIdAndRemindTimeIsNotNullAndDeleteTimeIsNull(idFromToken, pageable)
            false -> bookmarkRepository.findAllByUserIdAndDeleteTimeIsNull(idFromToken, pageable)
        }

        val bookmarkList = bookmarkToBookmarkRequestDto(list)
        val bookmarkInterfaceList = bookmarkInterfaceToBookmarkRequestDto(bookmarkInterfaceRepository.findAllByUserId(idFromToken))

        val result = bookmarkList + bookmarkInterfaceList
        return PageImpl(result, pageable, result.size.toLong())
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

    fun getAllPageByEncryptFolderId(token: String, pageable: Pageable): Page<BookmarkDto.BookmarkRequestDto> {
        val folderIdByString = aes256Util.decrypt(token)
        val bookmarkList =
            bookmarkToBookmarkRequestDto(bookmarkRepository.findAllByFolderIdAndDeleteTimeIsNull(folderIdByString.toLong()))
        val bookmarkInterfaceList =
            bookmarkInterfaceToBookmarkRequestDto(bookmarkInterfaceRepository.findAllByFolderId(folderIdByString.toLong()))

        val list = bookmarkList + bookmarkInterfaceList

        return PageImpl(list, pageable, list.size.toLong())
    }

    private fun bookmarkToBookmarkRequestDto(bookmarkList: List<Bookmark>): List<BookmarkDto.BookmarkRequestDto> {
        val result = mutableListOf<BookmarkDto.BookmarkRequestDto>()
        for (bookmark in bookmarkList) {
            result.add(
                BookmarkDto.BookmarkRequestDto(
                    bookmark.id,
                    bookmark.userId,
                    bookmark.link,
                    bookmark.title,
                    bookmark.description,
                    bookmark.image,
                    bookmark.folderId,
                    bookmark.folderEmoji,
                    bookmark.folderName,
                    bookmark.clickCount,
                    bookmark.deleteTime,
                    bookmark.deleted,
                    bookmark.saveTime,
                    null
                )
            )
        }
        return result
    }

    private fun bookmarkInterfaceToBookmarkRequestDto(bookmarkList: List<BookmarkInterface>): List<BookmarkDto.BookmarkRequestDto> {
        val result = mutableListOf<BookmarkDto.BookmarkRequestDto>()
        for (bookmark in bookmarkList) {
            result.add(
                BookmarkDto.BookmarkRequestDto(
                    bookmark.id,
                    bookmark.userId,
                    bookmark.link,
                    bookmark.title,
                    bookmark.description,
                    bookmark.image,
                    bookmark.folderId,
                    bookmark.folderEmoji,
                    bookmark.folderName,
                    bookmark.clickCount,
                    bookmark.deleteTime,
                    bookmark.deleted,
                    bookmark.saveTime,
                    null
                )
            )
        }
        return result
    }
}