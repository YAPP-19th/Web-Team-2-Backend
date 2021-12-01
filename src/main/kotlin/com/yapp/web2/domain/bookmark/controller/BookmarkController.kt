package com.yapp.web2.domain.bookmark.controller

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.service.BookmarkPageService
import com.yapp.web2.domain.bookmark.service.BookmarkSearchService
import com.yapp.web2.domain.bookmark.service.BookmarkService
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

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

    @GetMapping("/trash")
    fun getDeletedBookmarkPage(
        request: HttpServletRequest,
        pageable: Pageable,
        @RequestParam remind: Boolean
    ): ResponseEntity<Page<Bookmark>> {
        return ResponseEntity.status(HttpStatus.OK).body(bookmarkPageService.getAllPageByUserId(1, pageable, remind))
    }

    @PostMapping("/{folderId}")
    fun createBookmark(
        @PathVariable folderId: Long,
        @RequestBody bookmark: Bookmark.AddBookmarkDto
    ): ResponseEntity<String> {
        bookmarkService.addBookmark(folderId, bookmark)
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
        @RequestBody bookmark: Bookmark.UpdateBookmarkDto
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

    @GetMapping("/{userId}/{keyWord}")
    fun searchBookmarkList(
        @PathVariable userId: Long,
        @PathVariable keyWord: String,
        pageable: Pageable,
    ): ResponseEntity<Page<Bookmark>> {
        return ResponseEntity.status(HttpStatus.OK).body(bookmarkSearchService.searchKeywordOwnUserId(userId, keyWord, pageable))
    }
}