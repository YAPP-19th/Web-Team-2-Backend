package com.yapp.web2.domain.bookmark.controller

import com.yapp.web2.domain.bookmark.BookmarkDto
import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.service.BookmarkPageService
import com.yapp.web2.domain.bookmark.service.BookmarkSearchService
import com.yapp.web2.util.ControllerUtil
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/v1/page")
class BookmarkPageController(
    private val bookmarkPageService: BookmarkPageService,
    private val bookmarkSearchService: BookmarkSearchService
) {
    @GetMapping("/{folderId}")
    fun getBookmarkPage(
        @PathVariable folderId: Long,
        pageable: Pageable,
        @RequestParam remind: Boolean
    ): ResponseEntity<Page<BookmarkDto.BookmarkRequestDto>> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(bookmarkPageService.getAllPageByFolderId(folderId, pageable, remind))
    }

    @GetMapping("/main")
    fun getAllBookmarkPage(
        request: HttpServletRequest,
        pageable: Pageable,
        @RequestParam remind: Boolean
    ): ResponseEntity<Page<BookmarkDto.BookmarkRequestDto>> {
        val token = ControllerUtil.extractAccessToken(request)
        return ResponseEntity.status(HttpStatus.OK)
            .body(bookmarkPageService.getAllPageByUserId(token, pageable, remind))
    }

    @GetMapping("/trash")
    fun getTrashBookmarkPage(
        request: HttpServletRequest,
        pageable: Pageable,
        @RequestParam remind: Boolean
    ): ResponseEntity<Page<Bookmark>> {
        val token = ControllerUtil.extractAccessToken(request)
        return ResponseEntity.status(HttpStatus.OK)
            .body(bookmarkPageService.getTrashPageByUserId(token, pageable, remind))
    }

    @GetMapping("/search/{keyWord}")
    fun searchBookmarkList(
        request: HttpServletRequest,
        @PathVariable keyWord: String,
        pageable: Pageable,
    ): ResponseEntity<Page<Bookmark>> {
        val token = ControllerUtil.extractAccessToken(request)
        return ResponseEntity.status(HttpStatus.OK)
            .body(bookmarkSearchService.searchKeywordOwnUserId(token, keyWord, pageable))
    }

    @GetMapping("/today")
    fun getTodayRemindBookmarkList(
        request: HttpServletRequest
    ): ResponseEntity<BookmarkDto.RemindList> {
        val token = ControllerUtil.extractAccessToken(request)
        val todayRemindBookmarkList = bookmarkPageService.getTodayRemindBookmark(token)
        return ResponseEntity.status(HttpStatus.OK).body(todayRemindBookmarkList)
    }

    @GetMapping("/open/{folderToken}")
    fun getBookmarkPageByFolderToken(
        @PathVariable folderToken: String,
        pageable: Pageable
    ): ResponseEntity<Page<BookmarkDto.BookmarkRequestDto>> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(bookmarkPageService.getAllPageByEncryptFolderId(folderToken, pageable))
    }
}