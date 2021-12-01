package com.yapp.web2.domain.folder.controller

import com.yapp.web2.domain.folder.entity.Folder

import com.yapp.web2.domain.folder.service.FolderService
import com.yapp.web2.util.Message
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/v1/folder")
class FolderController(
    private val folderService: FolderService
) {
    @PostMapping
    fun createFolder(
        servletRequest: HttpServletRequest,
        @RequestBody request: Folder.FolderCreateRequest
    ): ResponseEntity<String> {
        val accessToken = servletRequest.getHeader("AccessToken")
        folderService.createFolder(request, accessToken)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @PatchMapping("/{folderId}/name")
    fun changeFolderName(
        @PathVariable folderId: Long,
        @RequestBody request: Folder.FolderNameChangeRequest
    ): ResponseEntity<String> {
        folderService.changeFolderName(folderId, request)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @PatchMapping("/{folderId}/emoji")
    fun changeFolderEmoji(
        @PathVariable folderId: Long,
        @RequestBody request: Folder.FolderEmojiChangeRequest
    ): ResponseEntity<String> {
        folderService.changeEmoji(folderId, request)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @PatchMapping("/{folderId}/move")
    fun moveFolder(
        @PathVariable folderId: Long,
        @RequestBody request: Folder.FolderMoveRequest
    ): ResponseEntity<String> {
        folderService.moveFolder(folderId, request)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @DeleteMapping("/{folderId}")
    fun deleteAllBookmarkAndFolder(@PathVariable folderId: Long): ResponseEntity<Any> {
        folderService.deleteAllBookmark(folderId)
        folderService.deleteFolder(folderId)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @GetMapping
    fun findAll(servletRequest: HttpServletRequest): ResponseEntity<Folder.FolderReadResponse> {
        val accessToken = servletRequest.getHeader("AccessToken")
        val response = folderService.findAll(accessToken)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

}