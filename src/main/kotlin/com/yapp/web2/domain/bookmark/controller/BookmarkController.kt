package com.yapp.web2.domain.bookmark.controller

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.service.BookmarkPageService
import com.yapp.web2.domain.bookmark.service.BookmarkSearchService
import com.yapp.web2.domain.bookmark.service.BookmarkService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/bookmark")
class BookmarkController(
    private val bookmarkPageService: BookmarkPageService,
    private val bookmarkService: BookmarkService,
    private val bookmarkSearchService: BookmarkSearchService
) {
    @GetMapping("/{folderId}")
    fun getBookmarkPage(
        @PathVariable folderId: Long,
        pageable: Pageable,
        @RequestParam remind: Boolean): ResponseEntity<Page<Bookmark>> {
        return ResponseEntity.status(HttpStatus.OK).body(bookmarkPageService.getAllPageByFolderId(folderId, pageable, remind))
    }

    @GetMapping("/click/{bookmarkId}")
    fun increaseBookmarkClickCount(@PathVariable bookmarkId: String): ResponseEntity<String> {
        bookmarkService.increaseBookmarkClickCount(bookmarkId)
        return ResponseEntity.status(HttpStatus.OK).body("올라감")
    }

    @PostMapping("/{folderId}")
    fun createBookmark(
        request: HttpServletRequest,
        @PathVariable folderId: Long,
        @RequestBody bookmark: Bookmark.AddBookmarkDto
    ): ResponseEntity<String> {
        val token = request.getHeader("AccessToken")
        bookmarkService.addBookmark(token, folderId, bookmark)
        return ResponseEntity.status(HttpStatus.OK).body("저장됨")
    }

    @DeleteMapping("/{bookmarkId}")
    fun deleteBookmark(@PathVariable bookmarkId: String): ResponseEntity<String> {
        bookmarkService.deleteBookmark(bookmarkId)
        return ResponseEntity.status(HttpStatus.OK).body("삭제됨")
    }

    @PatchMapping("/{bookmarkId}")
    fun updateBookmark(
        @PathVariable bookmarkId: String,
        @RequestBody @Valid bookmark: Bookmark.UpdateBookmarkDto
    ): ResponseEntity<String> {
        bookmarkService.updateBookmark(bookmarkId, bookmark)
        return ResponseEntity.status(HttpStatus.OK).body("업데이트됨")
    }

    @PatchMapping("/move/{bookmarkId}")
    fun moveBookmark(
        @PathVariable bookmarkId: String,
        @RequestBody bookmark: Bookmark.MoveBookmarkDto
    ): ResponseEntity<String> {
        bookmarkService.moveBookmark(bookmarkId, bookmark)
        return ResponseEntity.status(HttpStatus.OK).body("폴더가 이동됨")
    }

    @GetMapping("/search/{keyWord}")
    fun searchBookmarkList(
        request: HttpServletRequest,
        @PathVariable keyWord: String,
        pageable: Pageable,
    ): ResponseEntity<Page<Bookmark>> {
        val token = request.getHeader("AccessToken")
        return ResponseEntity.status(HttpStatus.OK).body(bookmarkSearchService.searchKeywordOwnUserId(token, keyWord, pageable))

    }
}