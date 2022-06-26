package com.yapp.web2.domain.bookmark.controller

import com.yapp.web2.domain.bookmark.BookmarkDto
import com.yapp.web2.domain.bookmark.service.PersonalBookmarkService
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
@RequestMapping("/api/v1/personalBookmark")
class PersonalBookmarkController(
    private val personalBookmarkService: PersonalBookmarkService
) {
    @ApiOperation(value = "북마크 삭제 API")
    @PostMapping("/delete")
    fun deleteBookmark(@RequestBody bookmarkList: BookmarkDto.BookmarkIdList): ResponseEntity<String> {
        personalBookmarkService.deleteBookmark(bookmarkList)
        return ResponseEntity.status(HttpStatus.OK).body(Message.DELETED)
    }

    @ApiOperation(value = "북마크 수정 API")
    @PatchMapping("/{bookmarkId}")
    fun updateBookmark(
        @PathVariable @ApiParam(value = "북마크 ID", example = "10", required = true) bookmarkId: String,
        @RequestBody @Valid @ApiParam(value = "북마크 수정 정보", required = true) dto: BookmarkDto.UpdateBookmarkDto
    ): ResponseEntity<String> {
        personalBookmarkService.updateBookmark(bookmarkId, dto)
        return ResponseEntity.status(HttpStatus.OK).body(Message.UPDATED)
    }

    @ApiOperation(value = "여러 북마크 이동 API")
    @PostMapping("/moveList")
    fun moveBookmarkList(
        @RequestBody @ApiParam(value = "북마크 이동 정보", required = true) moveBookmarkDto: BookmarkDto.MoveBookmarkDto
    ): ResponseEntity<String> {
        personalBookmarkService.moveBookmarkList(moveBookmarkDto)
        return ResponseEntity.status(HttpStatus.OK).body(Message.UPDATED)
    }

    @GetMapping("/remindOn/{bookmarkId}")
    fun toggleOnRemindBookmark(request: HttpServletRequest, @PathVariable bookmarkId: String): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        personalBookmarkService.toggleOnRemindBookmark(token, bookmarkId)
        return ResponseEntity.status(HttpStatus.OK).body(Message.UPDATED)
    }

    @GetMapping("/remindOff/{bookmarkId}")
    fun toggleOffRemindBookmark(request: HttpServletRequest, @PathVariable bookmarkId: String): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        personalBookmarkService.toggleOffRemindBookmark(token, bookmarkId)
        return ResponseEntity.status(HttpStatus.OK).body(Message.UPDATED)
    }
}