package com.yapp.web2.domain.bookmark.controller

import com.yapp.web2.domain.bookmark.BookmarkDto
import com.yapp.web2.domain.bookmark.service.BookmarkService
import com.yapp.web2.util.ControllerUtil
import com.yapp.web2.util.Message
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/bookmark")
class BookmarkController(
    private val bookmarkService: BookmarkService
) {
    @GetMapping("/click/{bookmarkId}")
    fun increaseBookmarkClickCount(@PathVariable bookmarkId: String): ResponseEntity<String> {
        bookmarkService.increaseBookmarkClickCount(bookmarkId)
        return ResponseEntity.status(HttpStatus.OK).body(Message.CLICK)
    }

    @PostMapping("/{folderId}")
    fun createBookmark(
        request: HttpServletRequest,
        @PathVariable @ApiParam(value = "북마크를 지정할 폴더 ID", example = "12", required = true) folderId: Long,
        @RequestBody @ApiParam(value = "북마크 생성 정보", required = true) bookmark: BookmarkDto.AddBookmarkDto
    ): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        bookmarkService.addBookmark(token, folderId, bookmark)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SAVED)
    }

    @ApiOperation(value = "북마크 생성 API")
    @PostMapping
    fun createBookmark(
        request: HttpServletRequest,
        @RequestParam @ApiParam(value = "북마크를 지정할 폴더 ID", example = "12", required = true) folderId: Long?,
        @RequestBody @ApiParam(value = "북마크 생성 정보", required = true) bookmark: BookmarkDto.AddBookmarkDto
    ): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        bookmarkService.addBookmark(token, folderId, bookmark)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SAVED)
    }

    @ApiOperation(value = "여러개의 북마크 생성 API")
    @PostMapping("/list")
    fun createBookmarkList(
        request: HttpServletRequest,
        @RequestParam(required = false) @ApiParam(value = "북마크가 저장될 폴더 ID", example = "12") folderId: Long?,
        @ApiParam(value = "북마크 생성 리스트 정보") @RequestBody dto: BookmarkDto.AddBookmarkListDto
    ): ResponseEntity<String> {
        println(folderId)
        val token = ControllerUtil.extractAccessToken(request)
        bookmarkService.addBookmarkList(token, folderId, dto)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SAVED)
    }

    @ApiOperation(value = "북마크 삭제 API")
    @PostMapping("/delete")
    fun deleteBookmark(@RequestBody bookmarkList: BookmarkDto.BookmarkIdList): ResponseEntity<String> {
        bookmarkService.deleteBookmark(bookmarkList)
        return ResponseEntity.status(HttpStatus.OK).body(Message.DELETED)
    }

    @ApiOperation(value = "북마크 수정 API")
    @PatchMapping("/{bookmarkId}")
    fun updateBookmark(
        @PathVariable @ApiParam(value = "북마크 ID", example = "10", required = true) bookmarkId: String,
        @RequestBody @Valid @ApiParam(value = "북마크 수정 정보", required = true) dto: BookmarkDto.UpdateBookmarkDto
    ): ResponseEntity<String> {
        bookmarkService.updateBookmark(bookmarkId, dto)
        return ResponseEntity.status(HttpStatus.OK).body(Message.UPDATED)
    }

    @ApiOperation(value = "여러 북마크 이동 API")
    @PostMapping("/moveList")
    fun moveBookmarkList(
        @RequestBody @ApiParam(value = "북마크 이동 정보", required = true) moveBookmarkDto: BookmarkDto.MoveBookmarkDto
    ): ResponseEntity<String> {
        bookmarkService.moveBookmarkList(moveBookmarkDto)
        return ResponseEntity.status(HttpStatus.OK).body(Message.UPDATED)
    }

    // TODO: 2022/05/03 이거 괜히 만들어져있는 거 같음 moveBookmarkDto에 list가 존재하기 때문에 굳이 bookmarkId를 pathvariable을 통해 받을 필요가 없다. 제거
    @ApiOperation(value = "북마크 이동 API")
    @PatchMapping("/move/{bookmarkId}")
    fun moveBookmark(
        @PathVariable @ApiParam(value = "북마크 ID", example = "10", required = true) bookmarkId: String,
        @RequestBody @ApiParam(value = "북마크 이동 정보", required = true) dto: BookmarkDto.MoveBookmarkDto
    ): ResponseEntity<String> {
        bookmarkService.moveBookmarkList(dto)
        return ResponseEntity.status(HttpStatus.OK).body(Message.MOVED)
    }

    @ApiOperation(value = "북마크 리마인드 On")
    @GetMapping("/remindOn/{bookmarkId}")
    fun toggleOnRemindBookmark(request: HttpServletRequest, @PathVariable bookmarkId: String): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        bookmarkService.toggleOnRemindBookmark(token, bookmarkId)
        return ResponseEntity.status(HttpStatus.OK).body(Message.UPDATED)
    }

    @ApiOperation(value = "북마크 리마인드 Off")
    @GetMapping("/remindOff/{bookmarkId}")
    fun toggleOffRemindBookmark(request: HttpServletRequest, @PathVariable bookmarkId: String): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        bookmarkService.toggleOffRemindBookmark(token, bookmarkId)
        return ResponseEntity.status(HttpStatus.OK).body(Message.UPDATED)
    }
}