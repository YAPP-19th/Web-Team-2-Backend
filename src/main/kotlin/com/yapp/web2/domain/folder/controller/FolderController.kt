package com.yapp.web2.domain.folder.controller

import com.yapp.web2.domain.account.AccountDto
import com.yapp.web2.domain.folder.entity.Folder

import com.yapp.web2.domain.folder.service.FolderService
import com.yapp.web2.util.ControllerUtil
import com.yapp.web2.util.FolderTokenDto
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
    ): ResponseEntity<Folder.FolderCreateResponse> {
        val accessToken = servletRequest.getHeader("AccessToken")
        val folderId = folderService.createFolder(request, accessToken).id
        return ResponseEntity.status(HttpStatus.OK).body(folderId?.let { Folder.FolderCreateResponse(it) })
    }

    @ApiOperation("폴더 수정")
    @PatchMapping("/{folderId}")
    fun updateFolder(
        @PathVariable @ApiParam(value = "폴더 ID", example = "4", required = true) folderId: Long,
        @RequestBody @ApiParam(value = "수정할 폴더 이름 및 이모지 정보", required = true) request: Folder.FolderChangeRequest
    ): ResponseEntity<String> {
        folderService.changeFolder(folderId, request)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation(value = "폴더 이동(드래그 & 드랍) API")
    @PatchMapping("/{folderId}/move")
    fun moveFolderDragAndDrop(
        servletRequest: HttpServletRequest,
        @PathVariable @ApiParam(value = "폴더 ID", example = "2", required = true) folderId: Long,
        @RequestBody @Valid @ApiParam(value = "이동할 폴더의 정보", required = true) request: Folder.FolderMoveRequest
    ): ResponseEntity<String> {
        val accessToken = ControllerUtil.extractAccessToken(servletRequest)
        folderService.moveFolderByDragAndDrop(folderId, request, accessToken)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation(value = "폴더 이동(버튼) API")
    @PatchMapping("/move")
    fun moveFolderByButton(
        servletRequest: HttpServletRequest,
        @RequestBody @ApiParam(
            value = "이동할 폴더들의 ID 및 다음 폴더 ID",
            required = true
        ) request: Folder.FolderMoveButtonRequest
    ): ResponseEntity<String> {
        val accessToken = ControllerUtil.extractAccessToken(servletRequest)
        folderService.moveFolderByButton(accessToken, request)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation(value = "폴더 삭제(삭제하려는 폴더와 하위에 존재하는 모든 폴더 및 북마크들 삭제) API")
    @DeleteMapping("/{folderId}")
    fun deleteAllBookmarkAndAllFolderWithRelatedFolder(
        @PathVariable @ApiParam(value = "폴더 ID", example = "2", required = true) folderId: Long
    ): ResponseEntity<String> {
        val folder = folderService.findByFolderId(folderId)
        folderService.deleteFolderRecursive(folder)
        folderService.deleteFolder(folder)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation(value = "폴더 조회 API", response = Folder.FolderFindAllResponse::class)
    @GetMapping
    fun findAll(servletRequest: HttpServletRequest): ResponseEntity<Map<String, Any>> {
        val accessToken = ControllerUtil.extractAccessToken(servletRequest)
        val response = folderService.findAll(accessToken)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @ApiOperation(value = "폴더 리스트 영구 삭제 API")
    @PostMapping("/deletes")
    fun deletePermanentFolderList(
        @RequestBody @ApiParam(value = "삭제할 폴더들의 ID 리스트", required = true) request: Folder.FolderListDeleteRequest
    ): ResponseEntity<String> {
        folderService.deleteFolderList(request)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation(value = "자식 폴더 리스트 조회 API")
    @GetMapping("/{folderId}/children")
    fun findFolderChildList(
        @PathVariable @ApiParam(value = "폴더 ID", example = "2", required = true) folderId: Long
    ): ResponseEntity<MutableList<Folder.FolderListResponse>> {
        return ResponseEntity.status(HttpStatus.OK).body(folderService.findFolderChildList(folderId))
    }

    @ApiOperation(value = "부모 폴더 리스트 조회 API")
    @GetMapping("/{folderId}/parent")
    fun findAllFolderParentList(
        @PathVariable @ApiParam(value = "폴더 ID", example = "2", required = true) folderId: Long
    ): ResponseEntity<MutableList<Folder.FolderListResponse>> {
        return ResponseEntity.status(HttpStatus.OK).body(folderService.findAllParentFolderList(folderId))
    }

    // TODO: 2022/06/22 유저 정보 확인
    @GetMapping("encrypt/{folderId}")
    fun getEncryptFolderId(@PathVariable folderId: Long): ResponseEntity<FolderTokenDto> {
        return ResponseEntity.status(HttpStatus.OK).body(folderService.encryptFolderId(folderId))
    }

    @ApiOperation(value = "보관함에 속한 유저 리스트 조회 API")
    @GetMapping("belong/{folderId}")
    fun getAccountList(@PathVariable @ApiParam(value = "폴더 ID", example = "2", required = true) folderId: Long):
        ResponseEntity<AccountDto.FolderBelongAccountListDto> {
        return ResponseEntity.status(HttpStatus.OK).body(folderService.getAccountListAtRootFolder(folderId))
    }
}