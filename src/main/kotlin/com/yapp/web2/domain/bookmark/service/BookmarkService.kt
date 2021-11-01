package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.BusinessException
import com.yapp.web2.exception.ObjectNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class BookmarkService(
    private val bookmarkRepository: BookmarkRepository,
    private val folderRepository: FolderRepository
) {
    @Transactional
    fun addBookmark(folderId: Long, bookmarkDto: Bookmark.AddBookmarkDto): Bookmark {
        // TODO: 토큰을 통해 userId 가져오기.
        val folder = checkFolderAbsence(folderId)
        val toSaveBookmark = bookmarkAddDtoToBookmark(bookmarkDto, folderId, userId = 1)
        checkSameBookmark(toSaveBookmark, folderId)

        folder.bookmarkCount++
        return bookmarkRepository.save(toSaveBookmark)
    }

    private fun checkFolderAbsence(folderId: Long): Folder {
        val folder = folderRepository.findById(folderId)
        if (folder.isEmpty) throw ObjectNotFoundException("해당 폴더가 존재하지 않습니다.")
        return folder.get()
    }

    private fun checkSameBookmark(bookmark: Bookmark, folderId: Long) {
        val bookmarkList = bookmarkRepository.findAllByFolderId(folderId)
        for (savedBookmark in bookmarkList) {
            if (savedBookmark.link == bookmark.link) throw BusinessException("똑같은 게 있어요.")
        }
    }

    private fun bookmarkAddDtoToBookmark(bookmarkDto: Bookmark.AddBookmarkDto, folderId: Long, userId: Long): Bookmark {
        lateinit var bookmark: Bookmark
        when (bookmarkDto.remind) {
            true -> bookmark = Bookmark(userId, folderId, bookmarkDto.url, bookmarkDto.title, remindTime = LocalDateTime.now())
        }
        return bookmark
    }

    @Transactional
    fun deleteBookmark(bookmarkId: Long) {
        val bookmark = getBookmarkIfPresent(bookmarkId)
        val folder = checkFolderAbsence(bookmark.folderId)

        folder.bookmarkCount--
        bookmark.deleteTime = LocalDateTime.now()
        bookmark.deleted = true
    }

    private fun getBookmarkIfPresent(bookmarkId: Long): Bookmark {
        val bookmark = bookmarkRepository.findById(bookmarkId)
        if (bookmark.isEmpty) throw BusinessException("없어요")
        return bookmark.get()
    }

    @Transactional
    fun updateBookmark(bookmarkId: Long, updateBookmarkDto: Bookmark.UpdateBookmarkDto): Bookmark {
        val toChangeBookmark = getBookmarkIfPresent(bookmarkId)

        updateBookmarkDto.let {
            toChangeBookmark.title = it.title
            when (updateBookmarkDto.remind) {
                true -> toChangeBookmark.remindTime = LocalDateTime.now()
            }
        }
        return toChangeBookmark
    }

    @Transactional
    fun increaseBookmarkClickCount(bookmarkId: Long): Bookmark {
        val bookmark = getBookmarkIfPresent(bookmarkId)
        bookmark.clickCount++
        return bookmark
    }

    @Transactional
    fun moveBookmark(prevFolderId: Long, nextFolderId: Long, bookmarkId: Long) {
        if (isSameFolder(prevFolderId, nextFolderId)) return
        val bookmark = getBookmarkIfPresent(bookmarkId)
        //TODO: count를 enum으로 변환할 것
        updateClickCountByFolderId(prevFolderId, -1)
        updateClickCountByFolderId(nextFolderId, 1)
        bookmark.folderId = nextFolderId
    }

    @Transactional
    protected fun updateClickCountByFolderId(folderId: Long, count: Int) {
        val folder = checkFolderAbsence(folderId)
        folder.bookmarkCount += count
    }

    fun isSameFolder(prevFolderId: Long, nextFolderId: Long) = prevFolderId == nextFolderId
}