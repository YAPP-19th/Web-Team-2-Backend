package com.yapp.web2.domain.folder.controller

import com.yapp.web2.domain.folder.entity.Folder

import com.yapp.web2.domain.folder.service.FolderService
import com.yapp.web2.util.Message
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/folder")
class FolderController(val folderService: FolderService) {

    // TODO: 2021/10/31 토큰에서 유저 찾기

    @PostMapping
    fun createFolder(
        @RequestBody request: Folder.FolderCreateRequest
    ): ResponseEntity<String> {
        folderService.createFolder(request)

        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @PatchMapping("/{folderId}")
    fun changeFolderName(
        @PathVariable folderId: Long,
        @RequestBody request: Folder.FolderNameChangeRequest
    ): ResponseEntity<String> {
        folderService.changeFolderName(folderId, request)

        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @PatchMapping("/move/{folderId}")
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

}