package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.entity.BookmarkDto
import com.yapp.web2.domain.bookmark.entity.BookmarkInterface
import com.yapp.web2.domain.bookmark.repository.BookmarkInterfaceRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.ObjectNotFoundException
import com.yapp.web2.exception.custom.BookmarkNotFoundException
import com.yapp.web2.exception.custom.FolderNotFoundException
import com.yapp.web2.exception.custom.SameBookmarkException
import com.yapp.web2.security.jwt.JwtProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PersonalBookmarkService(
    private val bookmarkInterfaceRepository: BookmarkInterfaceRepository,
    private val folderRepository: FolderRepository,
    private val jwtProvider: JwtProvider
) {

    fun addBookmark(token: String, folderId: Long?, bookmarkDto: BookmarkDto.AddBookmarkDto): BookmarkInterface {
        val account = jwtProvider.getAccountFromToken(token)
        val bookmark = BookmarkDto.AddBookmarkDtoToBookmark(bookmarkDto, account)

        folderId?.run {
            val folder = checkFolderAbsence(folderId)
            bookmark.changeFolderInfo(folder.id, folder.emoji, folder.name)
            checkSameBookmark(bookmark, folderId)
            folder.updateBookmarkCount(1)
        }

        return bookmarkInterfaceRepository.save(bookmark)
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

    @Transactional
    fun deleteBookmark(bookmarkIdList: BookmarkDto.BookmarkIdList) {
        val bookmarkList = bookmarkInterfaceRepository.findAllById(bookmarkIdList.dotoriIdList)
        val resultList = mutableListOf<BookmarkInterface>()

        for (bookmark in bookmarkList) {
            when (val folderId = bookmark.folderId) {
                null -> Unit
                else -> {
                    val folder = checkFolderAbsence(folderId)
                    folder.updateBookmarkCount(-1)
                }
            }
            bookmark.deleteBookmark()
            resultList.add(bookmark)
        }

        bookmarkInterfaceRepository.saveAll(resultList)
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
            bookmark.moveFolder(dto.nextFolderId)
            bookmark.changeFolderInfo(folder)
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

        bookmark.remindOff()
        bookmarkInterfaceRepository.save(bookmark)
    }
}