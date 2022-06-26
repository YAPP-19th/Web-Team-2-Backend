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

@RestController
@RequestMapping("/api/v1/bookmarkInterface")
class BookmarkInterfaceController(
    private val personalBookmarkService: PersonalBookmarkService
) {


    // TODO: 2022/06/26 로직 안에서 personal shared 나눠서 저장하기
    @ApiOperation(value = "여러개의 북마크 생성 API")
    @PostMapping
    fun createBookmarkList(
        request: HttpServletRequest,
        @RequestParam(required = false) @ApiParam(value = "북마크가 저장될 폴더 ID", example = "12") folderId: Long?,
        @ApiParam(value = "북마크 생성 리스트 정보") @RequestBody dto: BookmarkDto.AddBookmarkListDto
    ): ResponseEntity<String> {
        println(folderId)
        val token = ControllerUtil.extractAccessToken(request)
        personalBookmarkService.addBookmarkList(token, folderId, dto)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SAVED)
    }
}