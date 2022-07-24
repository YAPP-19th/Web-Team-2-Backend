package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.bookmark.BookmarkDto
import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.entity.Remind
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.Authority
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.custom.*
import com.yapp.web2.security.jwt.JwtProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SharedBookmarkService(
    private val bookmarkRepository: BookmarkRepository,
    private val folderRepository: FolderRepository,
    private val jwtProvider: JwtProvider
) {

    @Transactional(readOnly = true)
    fun checkAuthority(account: Account, folderId: Long) {
        var folder = folderRepository.findFolderById(folderId) ?: throw FolderNotFoundException()

        if (folder.rootFolderId != null) folder =
            folderRepository.findFolderById(folder.rootFolderId!!) ?: throw FolderNotFoundException()

        // 존재하면? 일단 공유 멤버이니까 return
        for (af in account.accountFolderList)
            if (af.folder == folder && af.authority > Authority.NONE) return

        throw NoPermissionException()
    }

    fun addBookmark(token: String, folderId: Long, bookmarkDto: BookmarkDto.AddBookmarkDto): Bookmark {
        val account = jwtProvider.getAccountFromToken(token)
        checkAuthority(account, folderId)

        val folder = checkFolderAbsence(folderId)
        val bookmark = BookmarkDto.addBookmarkDtoToBookmark(bookmarkDto, account)

        bookmark.changeFolderInfo(folder)
        checkSameBookmark(bookmark, folderId)
        folder.updateBookmarkCount(1)

        return bookmarkRepository.save(bookmark)
    }

    fun addBookmarkList(token: String, folderId: Long, dto: BookmarkDto.AddBookmarkListDto) {
        for (addBookmarkDto in dto.addBookmarkList)
            addBookmark(token, folderId, addBookmarkDto)
    }

    @Transactional(readOnly = true)
    fun checkFolderAbsence(folderId: Long): Folder {
        return folderRepository.findFolderById(folderId) ?: throw ObjectNotFoundException()
    }

    fun checkSameBookmark(bookmark: Bookmark, folderId: Long) {
        val bookmarkList = bookmarkRepository.findAllByFolderId(folderId)

        for (savedBookmark in bookmarkList) {
            if (savedBookmark.link == bookmark.link) throw SameBookmarkException()
        }
    }

    fun deleteBookmark(token: String, dto: BookmarkDto.SharedBookmarkDeleteDto) {
        val account = jwtProvider.getAccountFromToken(token)
        checkAuthority(account, dto.folderId)

        val bookmarkList = bookmarkRepository.findAllById(dto.dotoriIdList)
        val folder = checkFolderAbsence(dto.folderId)

        for (bookmark in bookmarkList) {
            bookmark.deleteBookmark()
            folder.updateBookmarkCount(-1)
        }

        bookmarkRepository.saveAll(bookmarkList)
    }

    fun updateBookmark(token: String, bookmarkId: String, dto: BookmarkDto.UpdateSharedBookmarkDto) {
        val account = jwtProvider.getAccountFromToken(token)
        checkAuthority(account, dto.folderId)

        val toChangeBookmark = bookmarkRepository.findBookmarkById(bookmarkId) ?: throw BookmarkNotFoundException()

        toChangeBookmark.updateBookmark(dto.title, dto.description)
        bookmarkRepository.save(toChangeBookmark)
    }

    fun moveBookmarkList(token: String, dto: BookmarkDto.MoveBookmarkDto) {
        val account = jwtProvider.getAccountFromToken(token)
        checkAuthority(account, dto.nextFolderId)
        checkSameRootFolder(dto.folderId, dto.nextFolderId)
        val bookmarkList = bookmarkRepository.findAllById(dto.bookmarkIdList)
        val folder = folderRepository.findFolderById(dto.nextFolderId) ?: throw FolderNotFoundException()

        for (bookmark in bookmarkList) {
            deleteBookmarkInfoAtFolder(bookmark)

            bookmark.moveFolder(dto.nextFolderId)
            bookmark.changeFolderInfo(folder)
            folder.updateBookmarkCount(1)
        }

        bookmarkRepository.saveAll(bookmarkList)
    }

    fun checkSameRootFolder(beforeFolderId: Long, nextFolderId: Long) {
        val beforeFolder = folderRepository.findFolderById(beforeFolderId) ?: throw FolderNotFoundException()
        val nextFolder = folderRepository.findFolderById(nextFolderId) ?: throw FolderNotFoundException()

        if(!beforeFolder.isFolderSameRootFolder(nextFolder)) throw NotSameRootFolderException()
    }

    fun deleteBookmarkInfoAtFolder(bookmark: Bookmark) {
        bookmark.folderId?.let {
            folderRepository.findFolderById(it)
        }?.run {
            this.updateBookmarkCount(-1)
        }
    }

    fun toggleOnRemindBookmark(token: String, bookmarkId: String) {
        val account = jwtProvider.getAccountFromToken(token)
        val bookmark =
            bookmarkRepository.findBookmarkById(bookmarkId) ?: throw BookmarkNotFoundException()
        val folderId = bookmark.folderId ?: throw RuntimeException("폴더에 속해있지 않습니다!")
        val remind = Remind(account)

        checkAuthority(account, folderId)

        bookmark.remindOn(remind)
        bookmarkRepository.save(bookmark)
    }

    fun toggleOffRemindBookmark(token: String, bookmarkId: String) {
        val account = jwtProvider.getAccountFromToken(token)
        val bookmark =
            bookmarkRepository.findBookmarkById(bookmarkId) ?: throw BookmarkNotFoundException()
        val folderId = bookmark.folderId ?: throw RuntimeException("폴더에 속해있지 않습니다!")
        checkAuthority(account, folderId)

        bookmark.remindOff(account.id!!)

        bookmarkRepository.save(bookmark)
    }
}