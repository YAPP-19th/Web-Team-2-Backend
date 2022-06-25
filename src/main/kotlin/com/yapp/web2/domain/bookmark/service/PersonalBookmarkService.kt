package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.BookmarkDto
import com.yapp.web2.domain.bookmark.entity.BookmarkInterface
import com.yapp.web2.domain.bookmark.repository.BookmarkInterfaceRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.custom.BookmarkNotFoundException
import com.yapp.web2.exception.custom.FolderNotFoundException
import com.yapp.web2.exception.custom.ObjectNotFoundException
import com.yapp.web2.exception.custom.SameBookmarkException
import com.yapp.web2.security.jwt.JwtProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PersonalBookmarkService(
    private val bookmarkInterfaceRepository: BookmarkInterfaceRepository,
    private val folderRepository: FolderRepository,
    private val jwtProvider: JwtProvider
) {

    fun addBookmark(token: String, folderId: Long?, bookmarkDto: BookmarkDto.AddBookmarkDto): BookmarkInterface {
        val account = jwtProvider.getAccountFromToken(token)
        val bookmark = BookmarkDto.addBookmarkDtoToPersonalBookmark(bookmarkDto, account)

        folderId?.run {
            val folder = checkFolderAbsence(folderId)
            bookmark.changeFolderInfo(folder.id, folder.emoji, folder.name)
            checkSameBookmark(bookmark, folderId)
            folder.updateBookmarkCount(1)
        }

        return bookmarkInterfaceRepository.save(bookmark)
    }

    fun addBookmarkList(token: String, folderId: Long?, dto: BookmarkDto.AddBookmarkListDto) {
        for (addBookmarkDto in dto.addBookmarkList)
            addBookmark(token, folderId, addBookmarkDto)
    }

    @Transactional(readOnly = true)
    fun checkFolderAbsence(folderId: Long): Folder {
        return folderRepository.findFolderById(folderId) ?: throw ObjectNotFoundException()
    }

    fun checkSameBookmark(bookmarkInterface: BookmarkInterface, folderId: Long) {
        val bookmarkList = bookmarkInterfaceRepository.findAllByFolderId(folderId) ?: throw RuntimeException()

        for (savedBookmark in bookmarkList) {
            if (savedBookmark.link == bookmarkInterface.link) throw SameBookmarkException()
        }
    }

    fun deleteBookmark(bookmarkIdList: BookmarkDto.BookmarkIdList) {
        val bookmarkList = bookmarkInterfaceRepository.findAllById(bookmarkIdList.dotoriIdList)

        for (bookmark in bookmarkList) {
            when (val folderId = bookmark.folderId) {
                null -> Unit
                else -> {
                    val folder = checkFolderAbsence(folderId)
                    folder.updateBookmarkCount(-1)
                }
            }
            bookmark.deleteBookmark()
        }

        bookmarkInterfaceRepository.saveAll(bookmarkList)
    }

    fun updateBookmark(bookmarkId: String, dto: BookmarkDto.UpdateBookmarkDto) {
        val bookmark =
            bookmarkInterfaceRepository.findBookmarkInterfaceById(bookmarkId) ?: throw BookmarkNotFoundException()

        bookmark.updateBookmark(dto.title, dto.description)
        bookmarkInterfaceRepository.save(bookmark)
    }

    fun moveBookmarkList(dto: BookmarkDto.MoveBookmarkDto) {
        val bookmarkList = bookmarkInterfaceRepository.findAllById(dto.bookmarkIdList)
        val folder = folderRepository.findFolderById(dto.nextFolderId) ?: throw FolderNotFoundException()

        for (bookmark in bookmarkList) {
            bookmark.folderId?.let {
                folderRepository.findFolderById(it)
            }?.run {
                this.updateBookmarkCount(-1)
            }

            bookmark.moveFolder(dto.nextFolderId)
            bookmark.changeFolderInfo(folder)
            folder.updateBookmarkCount(1)
        }

        bookmarkInterfaceRepository.saveAll(bookmarkList)
    }

    fun toggleOnRemindBookmark(token: String, bookmarkId: String) {
        val account = jwtProvider.getAccountFromToken(token)
        val bookmark =
            bookmarkInterfaceRepository.findBookmarkInterfaceById(bookmarkId) ?: throw BookmarkNotFoundException()

        bookmark.remindOn(account.remindCycle.toLong())
        bookmarkInterfaceRepository.save(bookmark)
    }

    fun toggleOffRemindBookmark(token: String, bookmarkId: String) {
        val bookmark =
            bookmarkInterfaceRepository.findBookmarkInterfaceById(bookmarkId) ?: throw BookmarkNotFoundException()

        if (!bookmark.parentBookmarkId.isNullOrBlank()) {
            bookmarkInterfaceRepository.delete(bookmark)
            return
        }

        bookmark.remindOff()
        bookmarkInterfaceRepository.save(bookmark)
    }
}