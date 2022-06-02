package com.yapp.web2.domain.bookmark.controller

import com.yapp.web2.domain.bookmark.entity.BookmarkDto
import com.yapp.web2.domain.bookmark.service.SharedBookmarkService
import com.yapp.web2.util.ControllerUtil
import com.yapp.web2.util.Message
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/sharedBookmark")
class SharedBookmarkController(
    private val sharedBookmarkService: SharedBookmarkService
) {
    @ApiOperation(value = "북마크 생성 API")
    @PostMapping("/{folderId}")
    fun createBookmark(
        request: HttpServletRequest,
        @PathVariable @ApiParam(value = "북마크를 지정할 폴더 ID", example = "12", required = true) folderId: Long,
        @RequestBody @ApiParam(value = "북마크 생성 정보", required = true) bookmark: BookmarkDto.AddBookmarkDto
    ): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        sharedBookmarkService.addBookmark(token, folderId, bookmark)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SAVED)
    }

    @ApiOperation(value = "북마크 삭제 API")
    @PostMapping("/delete")
    fun deleteBookmark(
        request: HttpServletRequest,
        @RequestBody bookmarkList: BookmarkDto.SharedBookmarkDeleteDto
    ): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        sharedBookmarkService.deleteBookmark(token, bookmarkList)
        return ResponseEntity.status(HttpStatus.OK).body(Message.DELETED)
    }

    @ApiOperation(value = "북마크 수정 API")
    @PatchMapping("/{bookmarkId}")
    fun updateBookmark(
        request: HttpServletRequest,
        @PathVariable @ApiParam(value = "북마크 ID", example = "10", required = true) bookmarkId: String,
        @RequestBody @Valid @ApiParam(value = "북마크 수정 정보", required = true) dto: BookmarkDto.UpdateSharedBookmarkDto
    ): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        sharedBookmarkService.updateBookmark(token, bookmarkId, dto)
        return ResponseEntity.status(HttpStatus.OK).body(Message.UPDATED)
    }

    @PostMapping("/moveList")
    fun moveBookmarkList(
        request: HttpServletRequest,
        @RequestBody moveBookmarkDto: BookmarkDto.MoveBookmarkDto
    ): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        sharedBookmarkService.moveBookmarkList(token, moveBookmarkDto)
        return ResponseEntity.status(HttpStatus.OK).body(Message.UPDATED)
    }

    @GetMapping("/remindOn/{bookmarkId}")
    fun toggleOnRemindBookmark(request: HttpServletRequest, @PathVariable bookmarkId: String): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        sharedBookmarkService.toggleOnRemindBookmark(token, bookmarkId)
        return ResponseEntity.status(HttpStatus.OK).body(Message.UPDATED)
    }

    @GetMapping("/remindOff/{bookmarkId}")
    fun toggleOffRemindBookmark(request: HttpServletRequest, @PathVariable bookmarkId: String): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        sharedBookmarkService.toggleOffRemindBookmark(token, bookmarkId)
        return ResponseEntity.status(HttpStatus.OK).body(Message.UPDATED)
    }
}