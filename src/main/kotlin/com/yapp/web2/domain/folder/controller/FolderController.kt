package com.yapp.web2.domain.folder.controller

import com.yapp.web2.domain.folder.entity.Folder

import com.yapp.web2.domain.folder.service.FolderService
import com.yapp.web2.util.Message
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/folder")
class FolderController(
    private val folderService: FolderService
) {
    @ApiOperation(value = "폴더 생성")
    @PostMapping
    fun createFolder(
        servletRequest: HttpServletRequest,
        @RequestBody @ApiParam(value = "폴더 생성 정보", required = true) request: Folder.FolderCreateRequest
    ): ResponseEntity<String> {
        val accessToken = servletRequest.getHeader("AccessToken")
        folderService.createFolder(request, accessToken)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation("폴더 이름 수정 API")
    @PatchMapping("/{folderId}/name")
    fun changeFolderName(
        @PathVariable @ApiParam(value = "폴더 ID", example = "2", required = true) folderId: Long,
        @RequestBody @ApiParam(value = "수정할 폴더명", required = true) request: Folder.FolderNameChangeRequest
    ): ResponseEntity<String> {
        folderService.changeFolderName(folderId, request)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation(value = "폴더 이모지 수정 API")
    @PatchMapping("/{folderId}/emoji")
    fun changeFolderEmoji(
        @PathVariable @ApiParam(value = "폴더 ID", example = "2", required = true) folderId: Long,
        @RequestBody @ApiParam(value = "수정할 이모지 이름", required = true) request: Folder.FolderEmojiChangeRequest
    ): ResponseEntity<String> {
        folderService.changeEmoji(folderId, request)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation(value = "폴더 이동(드래그 & 드랍) API")
    @PatchMapping("/{folderId}/move")
    fun moveFolder(
        @PathVariable @ApiParam(value = "폴더 ID", example = "2", required = true) folderId: Long,
        @RequestBody @Valid @ApiParam(value = "이동할 폴더의 정보", required = true) request: Folder.FolderMoveRequest
    ): ResponseEntity<String> {
        folderService.moveFolder(folderId, request)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation(value = "폴더 삭제 API")
    @PostMapping("/{folderId}")
    fun deleteAllBookmarkAndFolder(
        @PathVariable @ApiParam(value = "폴더 ID", example = "2", required = true) folderId: Long): ResponseEntity<String> {
        folderService.deleteAllBookmark(folderId)
        folderService.deleteFolder(folderId)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation(value = "폴더 조회 API", response = Folder.FolderFindAllResponse::class)
    @GetMapping
    fun findAll(servletRequest: HttpServletRequest): ResponseEntity<Map<String, Any>> {
        val accessToken = servletRequest.getHeader("AccessToken")
        val response = folderService.findAll(accessToken)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

}