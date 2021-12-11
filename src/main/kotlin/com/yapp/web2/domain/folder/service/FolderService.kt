package com.yapp.web2.domain.folder.service

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
import javax.transaction.Transactional

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

    // TODO: 리팩토링
    @Transactional
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

    @Transactional
    fun changeFolderName(id: Long, request: Folder.FolderNameChangeRequest): String {
        folderRepository.findByIdOrNull(id)
            ?.let { folder ->
                folder.name = request.name
                folderRepository.save(folder)
                return request.name
            }
            ?: throw folderNotFoundException
    }

    @Transactional
    fun moveFolder(id: Long, request: Folder.FolderMoveRequest, accessToken: String) {
        val userId = jwtProvider.getIdFromToken(accessToken)
        val user = accountRepository.findById(userId).get()
        val moveFolder = folderRepository.findById(id).get()
        val nextParentId = request.nextParentId.toString().toLongOrNull()
        var nextParentFolder: Folder? = null

        if (nextParentId != null) {
            nextParentFolder = folderRepository.findById(nextParentId).get()
        }

        /* 최상위 -> 최상위 && 상위 -> 상위(동일한 부모) */
        if ((moveFolder.parentFolder == null && nextParentId == null)
            || (moveFolder.parentFolder == nextParentFolder)
        ) {
            val folderMove: FolderMoveInnerStrategy =
                FolderMoveWithEqualParentOrTopFolder(request, moveFolder, folderRepository, user)
            folderMove.moveFolder()
            return
        }

        val folderMove = FolderMoveFactory.getFolderMove(request, moveFolder, folderRepository, user)
        folderMove.folderDragAndDrop(moveFolder, request.nextIndex)

    }

    @Transactional
    fun deleteAllBookmark(folderId: Long) {
        bookmarkRepository.findByFolderId(folderId)
            .let { list ->
                list.forEach {
                    it.deletedByFolder()
                    bookmarkRepository.save(it)
                }
            }
    }

    @Transactional
    fun deleteFolder(id: Long) {
        folderRepository.findById(id).ifPresent { folder ->
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
    @Transactional
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
            //filter { it.children != null }
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

    @Transactional
    fun changeEmoji(id: Long, request: Folder.FolderEmojiChangeRequest): String {
        folderRepository.findByIdOrNull(id)?.let { folder ->
            folder.emoji = request.emoji
            folderRepository.save(folder)
            return request.emoji
        } ?: throw folderNotFoundException
    }

    @Transactional
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


}
