package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.BusinessException
import com.yapp.web2.exception.ObjectNotFoundException
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
    @Transactional
    fun addBookmark(token: String, folderId: Long, bookmarkDto: Bookmark.AddBookmarkDto): Bookmark {
        val idFromToken = jwtProvider.getIdFromToken(token)
        val folder = checkFolderAbsence(folderId)
        val toSaveBookmark = bookmarkAddDtoToBookmark(bookmarkDto, folderId, idFromToken)
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
        return when (bookmarkDto.remind) {
            true -> Bookmark(userId, folderId, bookmarkDto.url, bookmarkDto.title, remindTime = LocalDate.now(), bookmarkDto.image, bookmarkDto.description)
            false -> Bookmark(userId, folderId, bookmarkDto.url, bookmarkDto.title, null, bookmarkDto.image, bookmarkDto.description)
        }
    }

    fun deleteBookmark(bookmarkId: String) {
        val bookmark = getBookmarkIfPresent(bookmarkId)
        val folder = bookmark.folderId?.let { checkFolderAbsence(it) }

        folder!!.bookmarkCount--
        bookmark.deleteTime = LocalDateTime.now()
        bookmark.deleted = true
        bookmarkRepository.save(bookmark)
    }

    private fun getBookmarkIfPresent(bookmarkId: String): Bookmark {
        val bookmark = bookmarkRepository.findById(bookmarkId)
        if (bookmark.isEmpty) throw BusinessException("없어요")
        return bookmark.get()
    }

    fun updateBookmark(bookmarkId: String, updateBookmarkDto: Bookmark.UpdateBookmarkDto): Bookmark {
        val toChangeBookmark = getBookmarkIfPresent(bookmarkId)

        updateBookmarkDto.let {
            toChangeBookmark.title = it.title
            when (updateBookmarkDto.remind) {
                true -> toChangeBookmark.remindTime = LocalDate.now()
                false -> toChangeBookmark.remindTime = null
            }
        }
        bookmarkRepository.save(toChangeBookmark)
        return toChangeBookmark
    }

    @Transactional
    fun increaseBookmarkClickCount(bookmarkId: String): Bookmark {
        val bookmark = getBookmarkIfPresent(bookmarkId)
        bookmark.clickCount++
        return bookmarkRepository.save(bookmark)
    }

    @Transactional
    fun moveBookmark(bookmarkId: String, moveBookmarkDto: Bookmark.MoveBookmarkDto) {
        if (isSameFolder(moveBookmarkDto.prevFolderId, moveBookmarkDto.nextFolderId)) return
        val bookmark = getBookmarkIfPresent(bookmarkId)
        //TODO: count를 enum으로 변환할 것
        updateClickCountByFolderId(moveBookmarkDto.prevFolderId, -1)
        updateClickCountByFolderId(moveBookmarkDto.nextFolderId, 1)
        bookmark.folderId = moveBookmarkDto.nextFolderId
        bookmarkRepository.save(bookmark)
    }

    @Transactional
    protected fun updateClickCountByFolderId(folderId: Long, count: Int) {
        val folder = checkFolderAbsence(folderId)
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
}