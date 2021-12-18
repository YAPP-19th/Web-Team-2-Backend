package com.yapp.web2.domain.folder.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.AccountFolder
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.domain.folder.service.move.factory.FolderMoveFactory
import com.yapp.web2.domain.folder.service.move.inner.FolderMoveInnerStrategy
import com.yapp.web2.domain.folder.service.move.inner.FolderMoveWithEqualParentOrTopFolder
import com.yapp.web2.exception.BusinessException
import com.yapp.web2.exception.custom.FolderNotFoundException
import com.yapp.web2.security.jwt.JwtProvider
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class FolderService(
    private val folderRepository: FolderRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val accountRepository: AccountRepository,
    private val jwtProvider: JwtProvider
) {
    companion object {
        private val folderNotFoundException = FolderNotFoundException()
    }

    fun createDefaultFolder(account: Account) {
        val defaultFolder = Folder("보관함1", index = 0, parentFolder = null)
        val folder = folderRepository.save(defaultFolder)
        val accountFolder = AccountFolder(account, folder)
        folder.folders?.add(accountFolder)
    }

    // TODO: 리팩토링
    fun createFolder(request: Folder.FolderCreateRequest, accessToken: String): Folder {
        val userId = jwtProvider.getIdFromToken(accessToken)
        val user = accountRepository.findByIdOrNull(userId)
        val folder: Folder

        when (isParentFolder(request.parentId)) {
            true -> {
                folder = Folder.dtoToEntity(request)
                val accountFolder = user?.let { AccountFolder(it, folder) }
                folder.folders?.add(accountFolder!!)
            }
            false -> {
                val parentFolder: Folder = folderRepository.findById(request.parentId).get()
                val childrenFolderList: MutableList<Folder>? = parentFolder.children

                childrenFolderList?.let {
                    if (isMaxFolderCount(it.size)) {
                        throw BusinessException("하위 폴더는 최대 8개까지 생성을 할 수 있습니다.")
                    }
                }
                folder = Folder.dtoToEntity(request, parentFolder)
                val accountFolder = user?.let { AccountFolder(it, folder) }
                parentFolder.folders?.add(accountFolder!!)
                childrenFolderList?.add(folder)
            }
        }
        return folderRepository.save(folder)
    }

    private fun isMaxFolderCount(size: Int) = size >= 8

    private fun isParentFolder(parentId: Long) = parentId == 0L

    fun changeFolderName(id: Long, request: Folder.FolderNameChangeRequest): String {
        folderRepository.findByIdOrNull(id)
            ?.let { folder ->
                folder.name = request.name
                folderRepository.save(folder)
                return request.name
            }
            ?: throw folderNotFoundException
    }

    fun moveFolderDragAndDrop(id: Long, request: Folder.FolderMoveRequest, accessToken: String) {
        val userId = jwtProvider.getIdFromToken(accessToken)
        val user = accountRepository.findById(userId).get()
        val moveFolder = folderRepository.findById(id).get()
        val nextParentId = request.nextParentId.toString().toLongOrNull()
        var nextParentFolder: Folder? = null

        if (nextParentId != null) {
            nextParentFolder = folderRepository.findById(nextParentId).get()
        }

        /* 최상위 -> 최상위 OR 상위 -> 상위(동일한 부모) */
        if (isMoveTopFolderToTopFolder(moveFolder, nextParentId)
            || isMoveFolderToFolderWithEqualParent(moveFolder, nextParentFolder)
        ) {
            val folderMove: FolderMoveInnerStrategy =
                FolderMoveWithEqualParentOrTopFolder(request, moveFolder, folderRepository, user)
            folderMove.moveFolder()
            return
        }

        val folderMove = FolderMoveFactory.getFolderMove(request, moveFolder, folderRepository, user)
        folderMove.folderDragAndDrop(moveFolder, request.nextIndex)
    }

    fun moveFolderButton(accessToken: String, request: Folder.FolderMoveButtonRequest) {
        val userId = jwtProvider.getIdFromToken(accessToken)
        val user = accountRepository.findById(userId).get()

        // 현재는 단일 폴더 이동만 가능해서 리스트가 반드시 1개로 요청
        val folderId = request.folderIdList[0]
        val beforeFolder = folderRepository.findById(folderId).orElseThrow { folderNotFoundException }

        val beforeChildren: MutableList<Folder> =
            when (beforeFolder.parentFolder) {
                null -> { // 이동하려는 폴더가 최상위 일경우
                    folderRepository.findAllByParentFolderIsNull(user)
                }
                else -> {
                    beforeFolder.parentFolder!!.children!!
                }
            }

        // 이전 폴더 리스트에서 이동하려는 폴더보다 index가 큰 폴더들 -1
        beforeChildren.stream()
            .filter { it.index > beforeFolder.index }
            .forEach {
                it.index--
                folderRepository.save(it)
            }

        val nextFolder = folderRepository.findById(request.nextFolderId).orElseThrow { folderNotFoundException }
        val nextFolderIndex = nextFolder.children?.size ?: 0
        beforeFolder.index = nextFolderIndex
        beforeFolder.parentFolder = nextFolder
    }

    private fun isMoveTopFolderToTopFolder(
        moveFolder: Folder,
        nextParentId: Long?
    ) = (moveFolder.parentFolder == null && nextParentId == null)

    private fun isMoveFolderToFolderWithEqualParent(
        moveFolder: Folder,
        nextParentFolder: Folder?
    ) = (moveFolder.parentFolder == nextParentFolder)

    fun deleteAllBookmark(folderId: Long) {
        bookmarkRepository.findByFolderId(folderId)
            .let { list ->
                list.forEach {
                    it.deletedByFolder()
                    it.remindOff()
                    bookmarkRepository.save(it)
                }
            }
    }

    fun deleteFolder(id: Long) {
        folderRepository.findByIdOrNull(id)?.let { folder ->
            folderRepository.deleteByFolder(folder)
        }
    }

    /* API
    {
	"rootId": "root",
	"items": {
		"root": {
			"id": "root",
			"children": [최상위 폴더 애들],
		},
		"1": { // folderId
			"id": "폴더 고유 아이디",
			"children": [자식애들]  // ex) ["1", "4", "3"]
			"data": { "name": "폴더 이름", "emoji": "f123" }
		},
		"2": { // folderId
			"id": "폴더 고유 아이디",
			"children": [자식애들]  // ex) ["1", "4", "3"]
			"data": { "name": "폴더 이름", "emoji": "f123" }
		} ...
    }
    */
    fun findAll(accessToken: String): Map<String, Any> {
        val rootId = jwtProvider.getIdFromToken(accessToken)
        val user = accountRepository.findById(rootId).get()
        val itemsValue = mutableMapOf<String, Any>()

        /* "root" 하위 데이터 */
        val rootFolderList: MutableList<Folder> = folderRepository.findAllByParentFolderIsNull(user)
        val rootFolders: MutableList<Long> = mutableListOf()
        rootFolderList.stream()
            .filter { it.parentFolder == null }
            .forEach { it.id?.let { folderId -> rootFolders.add(folderId) } }

        val root = Folder.FolderFindAllResponse.Root(children = rootFolders)
        itemsValue["root"] = root

        /* "folder" 하위 데이터 */
        val allFolderList: MutableList<Folder> = folderRepository.findAllByAccount(user)
        allFolderList.stream()
            .forEach { rootFolder ->
                val id = rootFolder.id
                val children: MutableList<Long> = mutableListOf()
                rootFolder.children?.forEach { children.add(it.id!!) }
                val emoji = rootFolder.emoji ?: ""
                val data = Folder.FolderFindAllResponse.RootFolderData(rootFolder.name, emoji)
                val folderValue = Folder.FolderFindAllResponse.RootFolder(id!!, children, data)
                itemsValue[id.toString()] = folderValue
            }

        val responseMap = mutableMapOf<String, Any>()
        responseMap["rootId"] = "root"
        responseMap["items"] = itemsValue

        return responseMap
    }

    fun changeEmoji(id: Long, request: Folder.FolderEmojiChangeRequest): String {
        folderRepository.findByIdOrNull(id)?.let { folder ->
            folder.emoji = request.emoji
            return request.emoji
        } ?: throw folderNotFoundException
    }

    fun deleteFolderList(request: Folder.FolderListDeleteRequest) {
        request.deleteFolderIdList
            .stream()
            .forEach { folderId ->
                bookmarkRepository.findByFolderId(folderId)
                    .let { list ->
                        list.forEach {
                            it.deletedByFolder()
                            bookmarkRepository.save(it)
                        }
                    }
                folderRepository.deleteById(folderId)
            }
    }

    @Transactional(readOnly = true)
    fun findFolderChildList(folderId: Long): MutableList<Folder.FolderListResponse> {
        val childList: MutableList<Folder.FolderListResponse> = mutableListOf()

        folderRepository.findByIdOrNull(folderId)?.let {
            it.children?.stream()
                ?.forEach { folder ->
                    childList.add(Folder.FolderListResponse(folder.id!!, folder.emoji!!, folder.name))
                }
        }
        return childList
    }

    @Transactional(readOnly = true)
    fun findAllParentFolderList(folderId: Long): MutableList<Folder.FolderListResponse>? {
        val childList: MutableList<Folder.FolderListResponse> = mutableListOf()
        val folder = folderRepository.findByIdOrNull(folderId)
        var parentFolder = folder

        while (true) {
            when (parentFolder) {
                null -> {
                    break
                }
                else -> {
                    childList.add(Folder.FolderListResponse(parentFolder.id!!, parentFolder.emoji!!, parentFolder.name))
                }
            }
            parentFolder = parentFolder.parentFolder
        }
        childList.reverse()
        return childList
    }

}
