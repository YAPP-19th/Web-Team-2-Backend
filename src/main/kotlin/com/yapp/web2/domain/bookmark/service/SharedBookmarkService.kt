package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.bookmark.BookmarkDto
import com.yapp.web2.domain.bookmark.entity.BookmarkInterface
import com.yapp.web2.domain.bookmark.entity.SharedBookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkInterfaceRepository
import com.yapp.web2.domain.folder.entity.Authority
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.ObjectNotFoundException
import com.yapp.web2.exception.custom.*
import com.yapp.web2.security.jwt.JwtProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SharedBookmarkService(
    private val bookmarkInterfaceRepository: BookmarkInterfaceRepository,
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

    fun addBookmark(token: String, folderId: Long, bookmarkDto: BookmarkDto.AddBookmarkDto): BookmarkInterface {
        val account = jwtProvider.getAccountFromToken(token)
        checkAuthority(account, folderId)

        val folder = checkFolderAbsence(folderId)
        val bookmark = BookmarkDto.addBookmarkDtoToSharedBookmark(bookmarkDto, account, folder)

        bookmark.changeFolderInfo(folder.id, folder.emoji, folder.name)
        checkSameBookmark(bookmark, folderId)
        folder.updateBookmarkCount(1)

        return bookmarkInterfaceRepository.save(bookmark)
    }

    fun addBookmarkList(token: String, folderId: Long, dto: BookmarkDto.AddBookmarkListDto) {
        for (addBookmarkDto in dto.addBookmarkList)
            addBookmark(token, folderId, addBookmarkDto)
    }

    @Transactional(readOnly = true)
    fun checkFolderAbsence(folderId: Long): Folder {
        return folderRepository.findFolderById(folderId) ?: throw ObjectNotFoundException()
    }

    fun checkSameBookmark(bookmarkInterface: BookmarkInterface, folderId: Long) {
        val bookmarkList = bookmarkInterfaceRepository.findAllByFolderId(folderId) ?: throw SameBookmarkException()

        for (savedBookmark in bookmarkList) {
            if (savedBookmark.link == bookmarkInterface.link) throw SameBookmarkException()
        }
    }

    fun deleteBookmark(token: String, dto: BookmarkDto.SharedBookmarkDeleteDto) {
        val account = jwtProvider.getAccountFromToken(token)
        checkAuthority(account, dto.folderId)

        val bookmarkList = bookmarkInterfaceRepository.findAllById(dto.dotoriIdList)
        val folder = checkFolderAbsence(dto.folderId)

        bookmarkInterfaceRepository.deleteAllByParentBookmarkId(dto.dotoriIdList)

        for (bookmark in bookmarkList) {
            bookmark.deleteBookmark()
            folder.updateBookmarkCount(-1)
        }

        bookmarkInterfaceRepository.saveAll(bookmarkList)
    }

    fun updateBookmark(token: String, bookmarkId: String, dto: BookmarkDto.UpdateSharedBookmarkDto) {
        val account = jwtProvider.getAccountFromToken(token)
        checkAuthority(account, dto.folderId)

        val bookmark = bookmarkInterfaceRepository.findBookmarkInterfaceById(bookmarkId) ?: throw BookmarkNotFoundException()

        bookmark.updateBookmark(dto.title, dto.description)
        bookmarkInterfaceRepository.save(bookmark)

        val bookmarkList = bookmarkInterfaceRepository.findAllByParentBookmarkId(bookmarkId)
        for (b in bookmarkList) b.updateBookmark(dto.title, dto.description)

        bookmarkInterfaceRepository.saveAll(bookmarkList)
    }

    fun moveBookmarkList(token: String, dto: BookmarkDto.MoveBookmarkDto) {
        val account = jwtProvider.getAccountFromToken(token)
        checkAuthority(account, dto.nextFolderId)
        val bookmarkList = bookmarkInterfaceRepository.findAllById(dto.bookmarkIdList)
        val folder = folderRepository.findFolderById(dto.nextFolderId) ?: throw FolderNotFoundException()

        for (bookmark in bookmarkList) {
            val sharedBookmark = bookmark as SharedBookmark
            if (!sharedBookmark.isSameRootFolderId(folder.rootFolderId)) throw NotSameRootFolderException()

            deleteBookmarkInfoAtFolder(bookmark)

            sharedBookmark.moveFolder(dto.nextFolderId)
            sharedBookmark.changeFolderInfo(folder)
            folder.updateBookmarkCount(1)
        }

        bookmarkInterfaceRepository.saveAll(bookmarkList)
    }

    fun deleteBookmarkInfoAtFolder(bookmark: SharedBookmark) {
        bookmark.folderId?.let {
            folderRepository.findFolderById(it)
        }?.run {
            this.updateBookmarkCount(-1)
        }
    }

    fun toggleOnRemindBookmark(token: String, bookmarkId: String) {
        val account = jwtProvider.getAccountFromToken(token)
        val bookmark =
            bookmarkInterfaceRepository.findBookmarkInterfaceById(bookmarkId) ?: throw BookmarkNotFoundException()
        val copyBookmark = BookmarkInterface.copyBookmark(account, bookmark)

        bookmark.remindOn(account.id!!)

        bookmarkInterfaceRepository.save(copyBookmark)
        bookmarkInterfaceRepository.save(bookmark)
    }

    fun toggleOffRemindBookmark(token: String, bookmarkId: String) {
        val account = jwtProvider.getAccountFromToken(token)
        val bookmark =
            bookmarkInterfaceRepository.findBookmarkInterfaceById(bookmarkId) ?: throw BookmarkNotFoundException()

        bookmarkInterfaceRepository.deleteByParentBookmarkIdAndUserId(bookmarkId, account.id!!)
        bookmark.remindOff(account.id!!)

        bookmarkInterfaceRepository.save(bookmark)
    }
}