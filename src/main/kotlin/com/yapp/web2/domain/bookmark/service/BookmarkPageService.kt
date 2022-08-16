package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.BookmarkDto
import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.service.FolderService
import com.yapp.web2.security.jwt.JwtProvider
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class BookmarkPageService(
    private val bookmarkRepository: BookmarkRepository,
    private val folderService: FolderService,
    private val jwtProvider: JwtProvider
) {

    fun getAllPageByFolderId(
        token: String,
        folderId: Long,
        pageable: Pageable,
        remind: Boolean
    ): Page<Bookmark> {
        val userId = jwtProvider.getIdFromToken(token)

        return when (remind) {
            true -> {
                val bookmarkList =
                    bookmarkRepository.findRemindBookmarkInFolder(userId, folderIdList = mutableListOf(folderId))
                PageImpl(bookmarkList, pageable, bookmarkList.size.toLong())
            }
            false -> bookmarkRepository.findAllByFolderIdAndDeletedIsFalse(folderId, pageable)
        }
    }

    fun getAllPageByEncryptFolderId(token: String, pageable: Pageable): Page<Bookmark> {
        val folderId = jwtProvider.getIdFromToken(token)
        val folder = folderService.findByFolderId(folderId)
        if(!folder.isOpenState()) throw RuntimeException("보관함이 조회잠금상태입니다. 조회할 수 없습니다!")
        return bookmarkRepository.findAllByFolderIdAndDeletedIsFalse(folderId, pageable)
    }

    fun getTrashPageByUserId(token: String, pageable: Pageable): Page<Bookmark> {
        val idFromToken = jwtProvider.getIdFromToken(token)

        return bookmarkRepository.findAllByUserIdAndDeletedIsTrue(idFromToken, pageable)
    }

    fun getAllPageByUserId(token: String, pageable: Pageable, remind: Boolean): Page<Bookmark> {
        val account = jwtProvider.getAccountFromToken(token)

        return when (remind) {
            true -> {
                val bookmarkList =
                    bookmarkRepository.findRemindBookmark(account.id!!)
                PageImpl(bookmarkList, pageable, bookmarkList.size.toLong())
            }
            false -> {
                val folderIdList = mutableListOf<Long>()

                for (af in account.accountFolderList)
                    folderIdList.addAll(getAllLowerFolderId(af.folder))

                val bookmarkList = bookmarkRepository.findAllBookmark(account.id!!, folderIdList)
                PageImpl(bookmarkList, pageable, bookmarkList.size.toLong())
            }
        }
    }

    fun getAllLowerFolderId(parentFolder: Folder): List<Long> {
        val folderIdList = mutableListOf<Long>()

        folderIdList.add(parentFolder.id!!)
        parentFolder.children?.let {
            for (folder in it)
                folderIdList.addAll(getAllLowerFolderId(folder))
        }
        return folderIdList
    }

    fun getTodayRemindBookmark(token: String): BookmarkDto.RemindList {
        val idFromToken = jwtProvider.getIdFromToken(token)
        val yesterday = LocalDate.now().minusDays(1).toString()

        return BookmarkDto.RemindList(
            bookmarkRepository.findTodayRemindBookmark(idFromToken, yesterday)
        )
    }


}