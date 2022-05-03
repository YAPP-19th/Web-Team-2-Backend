package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.entity.*
import com.yapp.web2.domain.bookmark.repository.BookmarkInterfaceRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.ObjectNotFoundException
import com.yapp.web2.exception.custom.BookmarkNotFoundException
import com.yapp.web2.security.jwt.JwtProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookmarkInterfaceService(
    private val bookmarkInterfaceRepository: BookmarkInterfaceRepository,
    private val folderRepository: FolderRepository,
    private val jwtProvider: JwtProvider
) {
    fun moveBookmarkList(moveBookmarkDto: BookmarkDto.MoveBookmarkDto) {
        val bookmarkIdList = moveBookmarkDto.bookmarkIdList
        for (bookmarkId in bookmarkIdList)
            moveBookmark(bookmarkId, moveBookmarkDto)
    }

    fun moveBookmark(bookmarkId: String, moveBookmarkDto: BookmarkDto.MoveBookmarkDto) {
        val bookmark = getBookmarkIfPresent(bookmarkId)
        val nextFolder = checkFolderAbsence(moveBookmarkDto.nextFolderId)

        bookmark.folderId?.let {
            if(isSameFolder(it, moveBookmarkDto.nextFolderId)) return
            val beforeFolder = checkFolderAbsence(it)
            updateBookmarkCountByFolderId(beforeFolder, -1)
        }

        bookmark.changeFolderInfo(nextFolder)
        updateBookmarkCountByFolderId(nextFolder, 1)

        bookmarkInterfaceRepository.save(bookmark)
    }

    @Transactional
    protected fun updateBookmarkCountByFolderId(folder: Folder, count: Int) {
        folder.bookmarkCount += count
    }

    private fun isSameFolder(prevFolderId: Long, nextFolderId: Long) = prevFolderId == nextFolderId

    private fun getBookmarkIfPresent(bookmarkId: String): BookmarkInterface {
        return bookmarkInterfaceRepository.findBookmarkInterfaceById(bookmarkId)
            ?: throw BookmarkNotFoundException()
    }
    private fun checkFolderAbsence(folderId: Long): Folder {
        return when (val folder = folderRepository.findFolderById(folderId)) {
            null -> throw ObjectNotFoundException()
            else -> folder
        }
    }

    fun remindToggleOn(token: String, bookmarkId: String) {
        val account = jwtProvider.getAccountFromToken(token)
        val bookmark = getBookmarkIfPresent(bookmarkId)

        if(bookmark is SharedBookmark) {
            bookmark.remindOn(account.id!!)
            val personalBookmark = BookmarkUtils.sharedBookmarkToPersonalBookmark(bookmark, account)
            personalBookmark.remindOn(account.remindCycle.toLong())
            bookmarkInterfaceRepository.save(personalBookmark)
        }

        if(bookmark is PersonalBookmark)
            bookmark.remindOn(account.remindCycle.toLong())
    }

    fun remindToggleOff(token: String, bookmarkId: String) {
        val account = jwtProvider.getAccountFromToken(token)
        val bookmark = getBookmarkIfPresent(bookmarkId)

        if(bookmark is SharedBookmark) {
            bookmark.remindOff(account.id!!)
            bookmarkInterfaceRepository.deleteBookmarkInterfacesByParentBookmarkIdAndUserId(bookmarkId, account.id!!)
        }

        if(bookmark is PersonalBookmark)
            bookmark.remindOff()
    }
}