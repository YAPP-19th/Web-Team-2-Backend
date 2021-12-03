package com.yapp.web2.domain.folder.service

import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.AccountFolder
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.domain.account.repository.UserRepository
import com.yapp.web2.exception.custom.FolderNotFoundException
import com.yapp.web2.security.jwt.JwtProvider
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class FolderService(
    private val folderRepository: FolderRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider
) {
    companion object {
        private val folderNotFoundException = FolderNotFoundException()
    }

    @Transactional
    fun createFolder(request: Folder.FolderCreateRequest, accessToken: String): Folder {
        return when (isParentFolder(request.parentId)) {
            true -> {
                val userId = jwtProvider.getIdFromToken(accessToken)
                val user = userRepository.findByIdOrNull(userId)
                val rootFolder = Folder.dtoToEntity(request)
                val accountFolder = user?.let { AccountFolder(it, rootFolder) }
                rootFolder.folders?.add(accountFolder!!)
                folderRepository.save(rootFolder)
            }
            false -> {
                val parentFolder = folderRepository.findById(request.parentId).get()
                val childrenFolderList = parentFolder.children
                val folder = Folder.dtoToEntity(request, parentFolder)
                childrenFolderList?.add(folder)

                return folder
            }
        }
    }

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

    /**
     * 폴더 이동(드래그 앤 드랍) 로직
     * 1) 이동 전 부모폴더 & 폴더리스트, 이동 후 부모폴더 & 폴더리스트, 이동 폴더 조회
     * 2) 이동 전 폴더 리스트에서 자신보다 index가 큰 폴더들은 각각 -1
     * 3) 이동 후 폴더 리스트에서 자신보다 index가 큰 폴더들은 각각 +1
     * 4) 이동 전 폴더 리스트에서 제거 & 이동 후 폴더 리스트에 추가
     * 5) 이동 전, 이동 후 폴더 리스트 초기화 & DB 저장
     */
    @Transactional
    fun moveFolder(id: Long, request: Folder.FolderMoveRequest) {
        // 1)
        val prevParentFolder = folderRepository.findByIdOrNull(request.prevParentId)
        val nextParentFolder = folderRepository.findByIdOrNull(request.nextParentId)
        val prevChildFolderList = prevParentFolder?.children
        val nextChildFolderList = nextParentFolder?.children
        val moveFolder = folderRepository.findByIdOrNull(id)

        // 2)
        folderRepository.findByIndexGreaterThanPrevFolder(prevParentFolder!!, request.prevIndex)
            ?.let {
                it.forEach { folder -> folder.index-- }
            }

        // 3)
        folderRepository.findByIndexGreaterThanNextFolder(nextParentFolder!!, request.nextIndex)
            ?.let {
                it.forEach { folder -> folder.index++ }
            }

        // TODO: 2021/12/02 예외 생각
        moveFolder?.updateIndexAndParentFolder(request.nextIndex, nextParentFolder)

        // 4)
        prevChildFolderList?.removeAt(request.prevIndex)
        moveFolder?.let { nextChildFolderList?.add(request.nextIndex, it) }

        // 5)
        prevParentFolder.children = prevChildFolderList
        nextParentFolder.children = nextChildFolderList
    }

    // TODO: 2021/11/13 MongoDB ID 타입 변경
    @Transactional
    fun deleteAllBookmark(id: Long) {
        bookmarkRepository.findByFolderId(id)
            .let { list ->
                list.forEach { it.deletedByFolder() }
            }
    }

    @Transactional
    fun deleteFolder(id: Long) {
        folderRepository.deleteById(id)
    }

    /* API
    {
	"rootId": "유저 고유 아이디",
	"items": {
		"root": {
			"id": "유저 고유 아이디",
			"rootFolders": [최상위 폴더 애들],
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
        val user = userRepository.findById(rootId).get()
        val itemsValue = mutableMapOf<String, Any>()

        /* "root" 하위 데이터 */
        val rootFolderList: MutableList<Folder> = folderRepository.findAllByParentFolderIsNull(user)
        val rootFolders: MutableList<Long> = mutableListOf()
        rootFolderList.stream()
            .filter { it.parentFolder == null }
            .forEach { it.id?.let { folderId -> rootFolders.add(folderId) } }

        val root = Folder.FolderReadResponse.Root(rootId, rootFolders)
        itemsValue[rootId.toString()] = root

        /* "folder" 하위 데이터 */
        val allFolderList: MutableList<Folder> = folderRepository.findAll()
        allFolderList.stream()
            //filter { it.children != null }
            .forEach { rootFolder ->
                val id = rootFolder.id
                val children: MutableList<Long> = mutableListOf()
                rootFolder.children?.forEach { children.add(it.id!!) }
                val emoji = rootFolder.emoji ?: ""
                val data = Folder.FolderReadResponse.RootFolderData(rootFolder.name, emoji)
                val folderValue = Folder.FolderReadResponse.RootFolder(id!!, children, data)
                itemsValue[id.toString()] = folderValue
            }

        val responseMap = mutableMapOf<String, Any>()
        responseMap["rootId"] = rootId
        responseMap["items"] = itemsValue

        return responseMap
    }

    fun changeEmoji(id: Long, request: Folder.FolderEmojiChangeRequest): String {
        folderRepository.findByIdOrNull(id)?.let { folder ->
            folder.emoji = request.emoji
            folderRepository.save(folder)
            return request.emoji
        } ?: throw folderNotFoundException
    }

}
