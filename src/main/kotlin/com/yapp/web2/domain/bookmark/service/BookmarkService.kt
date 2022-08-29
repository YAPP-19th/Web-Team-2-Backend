package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.BookmarkDto
import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.entity.Remind
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.custom.AlreadyExistRemindException
import com.yapp.web2.exception.custom.BookmarkNotFoundException
import com.yapp.web2.exception.custom.FolderNotFoundException
import com.yapp.web2.exception.custom.ObjectNotFoundException
import com.yapp.web2.exception.custom.SameBookmarkException
import com.yapp.web2.security.jwt.JwtProvider
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class BookmarkService(
    private val bookmarkRepository: BookmarkRepository,
    private val folderRepository: FolderRepository,
    private val jwtProvider: JwtProvider
) {

    companion object {
        private val bookmarkNotFoundException = BookmarkNotFoundException()
        private val log = LoggerFactory.getLogger(BookmarkService::class.java)
    }

    fun addBookmark(token: String, folderId: Long?, bookmarkDto: BookmarkDto.AddBookmarkDto): Bookmark {
        val account = jwtProvider.getAccountFromToken(token)
        var bookmark = BookmarkDto.addBookmarkDtoToBookmark(bookmarkDto, account)

        folderId?.run {
            require(folderId > 0) { "folderId must be greater than zero" }

            val folder = checkFolderAbsence(folderId)
            bookmark.changeFolderInfo(folder)
            checkSameBookmark(bookmark, folderId)
            folder.updateBookmarkCount(1)
        }

        return bookmarkRepository.save(bookmark)
    }

    fun addBookmarkList(token: String, folderId: Long?, dto: BookmarkDto.AddBookmarkListDto) {
        for (addBookmarkDto in dto.addBookmarkList)
            addBookmark(token, folderId, addBookmarkDto)
    }

    @Transactional(readOnly = true)
    fun checkFolderAbsence(folderId: Long): Folder {
        return folderRepository.findFolderById(folderId) ?: throw ObjectNotFoundException()
    }

    private fun checkSameBookmark(bookmark: Bookmark, folderId: Long) {
        val bookmarkList = bookmarkRepository.findAllByFolderId(folderId)

        for (savedBookmark in bookmarkList) {
            if (savedBookmark.link == bookmark.link) throw SameBookmarkException()
        }
    }

    @Transactional
    fun deleteBookmark(bookmarkIdList: BookmarkDto.BookmarkIdList) {
        val bookmarkList = bookmarkRepository.findAllById(bookmarkIdList.dotoriIdList)

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

        bookmarkRepository.saveAll(bookmarkList)
    }

    private fun getBookmarkIfPresent(bookmarkId: String): Bookmark {
        return bookmarkRepository.findBookmarkById(bookmarkId)
            ?: throw bookmarkNotFoundException
    }

    fun updateBookmark(bookmarkId: String, dto: BookmarkDto.UpdateBookmarkDto): Bookmark {
        val toChangeBookmark = getBookmarkIfPresent(bookmarkId)

        toChangeBookmark.updateBookmark(dto.title, dto.description)
        return bookmarkRepository.save(toChangeBookmark)
    }

    fun toggleOnRemindBookmark(token: String, bookmarkId: String) {
        val account = jwtProvider.getAccountFromToken(token)
        val remind = Remind(account)

        bookmarkRepository.findBookmarkById(bookmarkId)?.let {
            if (it.isRemindExist(remind)) {
                log.info("Remind already exist in bookmark => userId: ${account.id}, bookmarkId: $bookmarkId")
                throw AlreadyExistRemindException()
            }
            it.remindOn(remind)
            bookmarkRepository.save(it)
        } ?: run {
            log.error("Remind on failed. Bookmark not exist => userId: ${account.id}, bookmarkId: $bookmarkId")
            throw BookmarkNotFoundException()
        }
    }

    fun toggleOffRemindBookmark(token: String, bookmarkId: String) {
        val account = jwtProvider.getAccountFromToken(token)

        bookmarkRepository.findBookmarkById(bookmarkId)?.let {
            it.remindOff(account.id!!)
            bookmarkRepository.save(it)
        } ?: run {
            log.error("Remind off failed. Bookmark not exist => userId: ${account.id}, bookmarkId: $bookmarkId")
            throw BookmarkNotFoundException()
        }
    }

    fun increaseBookmarkClickCount(bookmarkId: String): Bookmark {
        val bookmark = getBookmarkIfPresent(bookmarkId)
        bookmark.hitClickCount()
        return bookmarkRepository.save(bookmark)
    }

    fun moveBookmarkList(dto: BookmarkDto.MoveBookmarkDto) {
        val bookmarkList = bookmarkRepository.findAllById(dto.bookmarkIdList)
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

        bookmarkRepository.saveAll(bookmarkList)
    }

    fun restoreBookmarks(bookmarkIdList: MutableList<String>?) {
        bookmarkIdList?.let {
            bookmarkIdList.forEach {
                val restoreBookmark = bookmarkRepository.findByIdOrNull(it)?.restore()
                bookmarkRepository.save(restoreBookmark!!)
            }
        }
    }

    fun deleteBookmarkPermanently(bookmarkIdList: MutableList<String>?) {
        bookmarkIdList?.let {
            bookmarkIdList.forEach {
                val bookmark = bookmarkRepository.findByIdOrNull(it)
                bookmark?.let { entity -> bookmarkRepository.delete(entity) }
            }
        }
    }
}