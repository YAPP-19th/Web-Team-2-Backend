package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.entity.Information
import com.yapp.web2.domain.bookmark.entity.InformationDto
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
    fun addBookmark(folderId: Long, informationDto: InformationDto): Bookmark {
        // TODO: 토큰을 통해 userId 가져오기.
        val folder = checkFolderAbsence(folderId)
        val toSaveInformation = informationDtoToInformation(informationDto, folder.bookmarkCount)
        checkSameInformation(toSaveInformation, folderId)

        folder.bookmarkCount++
        return bookmarkRepository.save(Bookmark(1, 1, toSaveInformation))
    }

    private fun checkFolderAbsence(folderId: Long): Folder {
        val folder = folderRepository.findById(folderId)
        if (folder.isEmpty) throw ObjectNotFoundException("해당 폴더가 존재하지 않습니다.")
        return folder.get()
    }

    private fun checkSameInformation(information: Information, folderId: Long) {
        val bookmarkList = bookmarkRepository.findAllByFolderId(folderId)
        for (bookmark in bookmarkList) {
            if (bookmark.information.link == information.link) throw BusinessException("똑같은 게 있어요.")
        }
    }

    private fun informationDtoToInformation(informationDto: InformationDto, order: Int): Information {
        var remindTime: LocalDateTime? = null
        if (informationDto.remind == true) remindTime = LocalDateTime.now()
        return Information(informationDto.url, informationDto.title, remindTime)
    }

    @Transactional
    fun deleteBookmark(bookmarkId: Long) {
        val bookmark = getBookmarkIfPresent(bookmarkId)
        val folder = checkFolderAbsence(bookmark.folderId)

        folder.bookmarkCount--
        bookmark.information.deleteTime = LocalDateTime.now()
        bookmark.information.deleted = true
    }

    private fun getBookmarkIfPresent(bookmarkId: Long): Bookmark {
        val bookmark = bookmarkRepository.findById(bookmarkId)
        if (bookmark.isEmpty) throw BusinessException("없어요")
        return bookmark.get()
    }

    @Transactional
    fun updateBookmark(bookmarkId: Long, updateBookmarkDto: Bookmark.UpdateBookmarkDto): Bookmark {
        val toChangeBookmark = getBookmarkIfPresent(bookmarkId)
        val information = toChangeBookmark.information

        updateBookmarkDto.let {
            information.title = it.title
            when (updateBookmarkDto.remind) {
                true -> information.remindTime = LocalDateTime.now()
            }
        }
        return toChangeBookmark
    }

    @Transactional
    fun increaseBookmarkClickCount(bookmarkId: Long): Bookmark {
        val bookmark = getBookmarkIfPresent(bookmarkId)
        bookmark.information.clickCount++
        return bookmark
    }

    @Transactional
    fun moveBookmark(prevFolderId: Long, nextFolderId: Long, bookmarkId: Long) {
        if (isSameFolder(prevFolderId, nextFolderId)) return
        val bookmark = getBookmarkIfPresent(bookmarkId)
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