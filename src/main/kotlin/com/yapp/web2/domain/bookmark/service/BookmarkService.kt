package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.ObjectNotFoundException
import com.yapp.web2.exception.custom.BookmarkNotFoundException
import com.yapp.web2.exception.custom.SameBookmarkException
import com.yapp.web2.security.jwt.JwtProvider
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class BookmarkService(
    private val bookmarkRepository: BookmarkRepository,
    private val folderRepository: FolderRepository,
    private val jwtProvider: JwtProvider
) {

    companion object {
        private val bookmarkNotFoundException = BookmarkNotFoundException()
    }

    @Transactional
    fun addBookmark(token: String, folderId: Long?, bookmarkDto: Bookmark.AddBookmarkDto): Bookmark {
        val account = jwtProvider.getAccountFromToken(token)
        var toSaveBookmark = bookmarkAddDtoToBookmarkWithNoFolder(bookmarkDto, account)

        folderId?.run {
            val folder = checkFolderAbsence(folderId)
            toSaveBookmark = bookmarkAddDtoToBookmark(bookmarkDto, folder, account)
            checkSameBookmark(toSaveBookmark, folderId)
            folder.bookmarkCount++
        }

        return bookmarkRepository.save(toSaveBookmark)
    }

    private fun checkFolderAbsence(folderId: Long): Folder {
        return when (val folder = folderRepository.findFolderById(folderId)) {
            null -> throw ObjectNotFoundException()
            else -> folder
        }
    }

    private fun checkSameBookmark(bookmark: Bookmark, folderId: Long) {
        val bookmarkList = bookmarkRepository.findAllByFolderId(folderId)
        for (savedBookmark in bookmarkList) {
            if (savedBookmark.link == bookmark.link) throw SameBookmarkException()
        }
    }

    private fun bookmarkAddDtoToBookmark(
        bookmarkDto: Bookmark.AddBookmarkDto,
        folder: Folder,
        account: Account
    ): Bookmark {
        return when (bookmarkDto.remind) {
            true -> Bookmark(
                account.id!!,
                folder.id!!,
                folder.emoji!!,
                folder.name,
                bookmarkDto.url,
                bookmarkDto.title,
                remindTime = LocalDate.now().plusDays(account.remindCycle!!.toLong()).toString(),
                bookmarkDto.image,
                bookmarkDto.description
            )
            false -> Bookmark(
                account.id!!,
                folder.id!!,
                folder.emoji!!,
                folder.name,
                bookmarkDto.url,
                bookmarkDto.title,
                null,
                bookmarkDto.image,
                bookmarkDto.description
            )
        }
    }

    private fun bookmarkAddDtoToBookmarkWithNoFolder(bookmarkDto: Bookmark.AddBookmarkDto, account: Account): Bookmark {
        return when (bookmarkDto.remind) {
            true -> Bookmark(
                account.id!!,
                null,
                null,
                null,
                bookmarkDto.url,
                bookmarkDto.title,
                remindTime = LocalDate.now().plusDays(account.remindCycle!!.toLong()).toString(),
                bookmarkDto.image,
                bookmarkDto.description
            )
            false -> Bookmark(
                account.id!!,
                null,
                null,
                null,
                bookmarkDto.url,
                bookmarkDto.title,
                null,
                bookmarkDto.image,
                bookmarkDto.description
            )
        }
    }

    @Transactional
    fun deleteBookmark(bookmarkList: Bookmark.BookmarkIdList) {
        for (bookmarkId in bookmarkList.idList) {
            // TODO: 2021/12/18 bookmark 예외처리 메소드 안에서 진행하는 게 좋을 거 같음
            val bookmark = getBookmarkIfPresent(bookmarkId)

            when (val folderId = bookmark.folderId) {
                null -> Unit
                else -> {
                    val folder = checkFolderAbsence(folderId)
                    folder.bookmarkCount--
                }
            }

            bookmark.deleteTime = LocalDateTime.now()
            bookmark.deleted = true
            bookmarkRepository.save(bookmark)
        }
    }

    private fun getBookmarkIfPresent(bookmarkId: String): Bookmark {
        return bookmarkRepository.findBookmarkById(bookmarkId)
            ?: throw bookmarkNotFoundException
    }

    fun updateBookmark(token: String, bookmarkId: String, updateBookmarkDto: Bookmark.UpdateBookmarkDto): Bookmark {
        val toChangeBookmark = getBookmarkIfPresent(bookmarkId)
        val account = jwtProvider.getAccountFromToken(token)

        updateBookmarkDto.let {
            toChangeBookmark.title = it.title
            when (updateBookmarkDto.remind) {
                true -> toChangeBookmark.remindTime =
                    LocalDate.now().plusDays(account.remindCycle!!.toLong()).toString()
                false -> toChangeBookmark.remindTime = null
            }
        }

        return bookmarkRepository.save(toChangeBookmark)
    }

    fun increaseBookmarkClickCount(bookmarkId: String): Bookmark {
        val bookmark = getBookmarkIfPresent(bookmarkId)
        bookmark.clickCount++
        return bookmarkRepository.save(bookmark)
    }

    @Transactional
    fun moveBookmark(bookmarkId: String, moveBookmarkDto: Bookmark.MoveBookmarkDto) {
        var bookmark = getBookmarkIfPresent(bookmarkId)
        val nextFolder = checkFolderAbsence(moveBookmarkDto.nextFolderId)

        bookmark = when (bookmark.folderId) {
            null -> {
                changeBookmarkInfo(bookmark, nextFolder)
            }
            else -> {
                if (isSameFolder(bookmark.folderId!!, moveBookmarkDto.nextFolderId)) return
                val nowFolder = checkFolderAbsence(bookmark.folderId!!)
                updateClickCountByFolderId(nowFolder, -1)
                changeBookmarkInfo(bookmark, nextFolder)
            }
        }
        bookmarkRepository.save(bookmark)
    }

    private fun changeBookmarkInfo(bookmark: Bookmark, folder: Folder): Bookmark {
        bookmark.folderId = folder.id
        bookmark.folderName = folder.name
        bookmark.folderEmoji = folder.emoji!!
        updateClickCountByFolderId(folder, 1)
        return bookmark
    }

    fun moveBookmarkList(moveBookmarkDto: Bookmark.MoveBookmarkDto) {
        val bookmarkIdList = moveBookmarkDto.bookmarkIdList
        for (bookmarkId in bookmarkIdList)
            moveBookmark(bookmarkId, moveBookmarkDto)
    }

    @Transactional
    protected fun updateClickCountByFolderId(folder: Folder, count: Int) {
        folder.bookmarkCount += count
    }

    fun isSameFolder(prevFolderId: Long, nextFolderId: Long) = prevFolderId == nextFolderId

    @Transactional
    fun restore(bookmarkIdList: MutableList<String>?) {
        bookmarkIdList?.let {
            bookmarkIdList.forEach {
                // TODO: 2021/12/02  Bookmark 예외처리
                val restoreBookmark = bookmarkRepository.findByIdOrNull(it)?.restore()
                bookmarkRepository.save(restoreBookmark!!)
            }
        }
    }

    @Transactional
    fun permanentDelete(bookmarkIdList: MutableList<String>?) {
        bookmarkIdList?.let {
            bookmarkIdList.forEach {
                val bookmark = bookmarkRepository.findByIdOrNull(it)
                bookmark?.let { entity -> bookmarkRepository.delete(entity) }
            }
        }
    }

    fun releaseRemindBookmark(bookmarkId: String) {
        bookmarkRepository.findByIdOrNull(bookmarkId)
            ?.let {
                it.remindTime = null
                bookmarkRepository.save(it)
            } ?: bookmarkNotFoundException
    }
}