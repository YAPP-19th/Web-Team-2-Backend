package com.yapp.web2.domain.folder.service

import com.yapp.web2.domain.account.AccountDto
import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.FolderDto
import com.yapp.web2.domain.folder.entity.AccountFolder
import com.yapp.web2.domain.folder.entity.Authority
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.entity.SharedType
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.domain.folder.service.move.factory.FolderMoveFactory
import com.yapp.web2.domain.folder.service.move.inner.FolderMoveInnerStrategy
import com.yapp.web2.domain.folder.service.move.inner.FolderMoveWithEqualParentOrTopFolder
import com.yapp.web2.exception.custom.AccountNotFoundException
import com.yapp.web2.exception.custom.FolderNotFoundException
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.util.FolderTokenDto
import org.slf4j.LoggerFactory
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
        private val log = LoggerFactory.getLogger(FolderService::class.java)
    }

    fun createDefaultFolder(account: Account) {
        val defaultFolder = Folder("보관함1", index = 0, parentFolder = null)
        val folder = folderRepository.save(defaultFolder)
        val accountFolder = AccountFolder(account, folder)
        accountFolder.authority = Authority.ADMIN
        folder.folders?.add(accountFolder)
    }

    fun createFolder(request: Folder.FolderCreateRequest, accessToken: String): Folder {
        require(request.parentId >= 0) { "parentId must be greater than or equal to zero" }

        val accountId = jwtProvider.getIdFromToken(accessToken)
        val account = accountRepository.findById(accountId).orElseThrow { AccountNotFoundException() }
        val folder = when (isParentFolder(request.parentId)) {
            true -> {
                makeRootFolder(accountId, account, request)
            }
            false -> {
                makeChildFolder(accountId, account, request)
            }
        }
        return folderRepository.save(folder)
    }

    // 보관함(최상위 폴더일 때) -> index만 설정
    private fun makeRootFolder(
        accountId: Long,
        account: Account,
        request: Folder.FolderCreateRequest
    ): Folder {
        require(accountId > 0) { "accountId must be greater than zero" }
        require(request.parentId >= 0) { "parentId must be greater than or equal to zero" }

        val index = folderRepository.findAllByParentFolderCount(accountId)
        val folder = Folder.dtoToEntity(request, index)
        val accountFolder = AccountFolder(account, folder)
        accountFolder.authority = Authority.ADMIN
        folder.folders?.add(accountFolder)

        return folder
    }

    // 폴더 일 때 -> 부모 폴더 찾아서 설정 및 index 설정
    private fun makeChildFolder(
        accountId: Long,
        account: Account,
        request: Folder.FolderCreateRequest
    ): Folder {
        require(accountId > 0) { "accountId must be greater than zero" }
        require(request.parentId >= 0) { "parentId must be greater than or equal to zero" }

        val parentFolder: Folder = folderRepository.findById(request.parentId).get()
        val index = folderRepository.findAllByFolderCount(accountId, request.parentId)
        val folder = Folder.dtoToEntity(request, parentFolder, index)
        val accountFolder = AccountFolder(account, folder)
        parentFolder.folders?.add(accountFolder)

        return folder
    }

    private fun isParentFolder(parentId: Long) = parentId == 0L

    fun changeFolder(folderId: Long, request: Folder.FolderChangeRequest) {
        require(folderId > 0) { "folderId must be greater than zero" }

        folderRepository.findByIdOrNull(folderId)?.let { folder ->
            request.name?.let { requestName ->
                folder.name = requestName
                // 폴더 하위에 존재하는 모든 북마크의 폴더 이름 수정
                bookmarkRepository.findByFolderId(folderId).let { bookmarkList ->
                    bookmarkList.stream().forEach { bookmark ->
                        bookmark.folderName = request.name
                        bookmarkRepository.save(bookmark)
                    }
                }
            }

            request.emoji?.let { requestEmoji ->
                folder.emoji = requestEmoji
                // 폴더 하위에 존재하는 모든 북마크의 폴더 이모지 수정
                bookmarkRepository.findByFolderId(folderId).let { bookmarkList ->
                    bookmarkList.stream().forEach { bookmark ->
                        bookmark.folderEmoji = request.emoji
                        bookmarkRepository.save(bookmark)
                    }
                }
            }
        }
    }

    /**
     * 폴더 드래그 & 드랍 이동, 케이스는 다음 5가지
     *  1. 최상위 폴더 -> 상위 폴더 이동
     *  2. 상위 폴더 -> 최상위 폴더 이동
     *  3. 상위 폴더 -> 상위 폴더 이동
     *  4. 최상위 폴더 -> 최상위 폴더 이동
     *  5. 상위 폴더 -> 상위 폴더 이동
     * @param folderId: 이동할 폴더 ID
     * @param request: 이동 후 부모 폴더 & 이동 후 폴더 Index
     * @param accessToken: Jwt Token
     */
    fun moveFolderByDragAndDrop(folderId: Long, request: Folder.FolderMoveRequest, accessToken: String) {
        require(folderId >= 0) { "folderId must be greater than or equal to zero" }
        require(request.nextIndex >= 0) { "nextIndex must be greater than or equal to zero" }

        val userId = jwtProvider.getIdFromToken(accessToken)
        val user = accountRepository.findById(userId).get()
        val moveFolder = folderRepository.findById(folderId).get()
        val nextParentId = request.nextParentId.toString().toLongOrNull()
        var nextParentFolder: Folder? = null
        nextParentFolder = nextParentId?.let {
            folderRepository.findById(nextParentId).orElseThrow { FolderNotFoundException() }
        }

        /* 최상위 -> 최상위 OR 상위 -> 상위(동일한 부모) */
        if (isMoveTopFolderToTopFolder(moveFolder, nextParentId) ||
            isMoveFolderToFolderWithEqualParent(moveFolder, nextParentFolder)
        ) {
            val folderMove: FolderMoveInnerStrategy =
                FolderMoveWithEqualParentOrTopFolder(request, moveFolder, folderRepository, user)
            folderMove.moveFolder()
            return
        }

        val folderMove = FolderMoveFactory.getFolderMove(request, moveFolder, folderRepository, user)
        folderMove.folderDragAndDrop(moveFolder, request.nextIndex)
    }

    private fun isMoveTopFolderToTopFolder(
        moveFolder: Folder,
        nextParentId: Long?
    ) = (moveFolder.parentFolder == null && nextParentId == null)

    private fun isMoveFolderToFolderWithEqualParent(
        moveFolder: Folder,
        nextParentFolder: Folder?
    ) = (moveFolder.parentFolder == nextParentFolder)

    fun moveFolderByButton(accessToken: String, request: Folder.FolderMoveButtonRequest) {
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

        // 이동 전 폴더 리스트에서 자신보다 index가 큰 폴더들 index - 1
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

    @Transactional(readOnly = true)
    fun findByFolderId(folderId: Long): Folder {
        return folderRepository.findById(folderId).orElseThrow { folderNotFoundException }
    }

    fun deleteFolder(folder: Folder) {
        folderRepository.deleteByFolder(folder)
    }

    fun deleteFolderRecursive(folder: Folder) {
        // Base Condition: 최하위 Depth의 폴더
        val children: MutableList<Folder> = folder.children ?: return

        children.let { folderList ->
            folderList.stream().forEach { folder ->
                deleteFolderRecursive(folder)
            }
        }

        bookmarkRepository.findByFolderId(folder.id!!)
            .let { list ->
                list.forEach {
                    it.deletedByFolder()
                    it.remindOff()
                    bookmarkRepository.save(it)
                }
            }

        folderRepository.deleteByFolder(folder)
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
        require(folderId > 0) { "folderId must be greater than zero" }

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
        require(folderId > 0) { "folderId must be greater than zero" }

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

    fun inverseFolderStatus(folderId: Long) {
        val folder = folderRepository.findFolderById(folderId) ?: throw FolderNotFoundException()
        folder.inverseShareType()
    }

    fun getFolderLink(folderId: Long, sharedType: SharedType): FolderTokenDto {
        val folder = folderRepository.findFolderById(folderId) ?: throw FolderNotFoundException()
        folder.updateSharedType(sharedType)

        // 사용자가 folder의 링크를 edit이 가능한 상태로 받으려고 할 때, folder가 공유 상태가 아니라면 공유로 바꾼다.
        if (sharedType == SharedType.EDIT && !folder.isSharedFolder()) changeRootFolderToShared(folder, folderId)

        return FolderTokenDto(jwtProvider.createFolderToken(folderId, sharedType))
    }

    private fun changeRootFolderToShared(folder: Folder, rootFolderId: Long) {
        if (!folder.isRootFolder()) throw RuntimeException("보관함이 아닙니다! 공유를 할 수 없습니다.")

        val sharedFolderIdList = changeFolderToShared(folder, rootFolderId)
        changeBookmarkToShared(sharedFolderIdList)
    }

    private fun changeBookmarkToShared(sharedFolderIdList: List<Long?>) {
        val bookmarkList = bookmarkRepository.findAllByFolderId(sharedFolderIdList)

        for (bookmark in bookmarkList)
            bookmark.changeSharedTrue()
    }

    private fun changeFolderToShared(folder: Folder, rootFolderId: Long): List<Long?> {
        val folderIdList = mutableListOf<Long?>()
        folder.folderToShared(rootFolderId)
        folderIdList.add(folder.id)

        folder.children?.let {
            for (child in it) {
                folderIdList.addAll(changeFolderToShared(child, rootFolderId))
            }
        }

        return folderIdList
    }

    fun getAccountListAtRootFolder(folderId: Long): AccountDto.FolderBelongAccountListDto {
        require(folderId > 0) { "folderId must be greater than zero" }

        // 사용자가 account-folder에 속하여있지 않다면, 예외를 던진다. -> 필요할까?
        // folder를 통하여 account list를 받아와서 account를 dto로 변환한 리스트를 return한다.
        val folder = folderRepository.findFolderById(folderId) ?: throw FolderNotFoundException()
        val accountList = mutableListOf<AccountDto.FolderBelongAccountInfo>()

        folder.folders?.forEach {
            accountList.add(AccountDto.accountToFolderBelongAccountInfo(it.account))
        }
        return AccountDto.FolderBelongAccountListDto(accountList)
    }

    fun getFolderInfo(folderToken: String): FolderDto.FolderInfoDto {
        val folderId = jwtProvider.getIdFromToken(folderToken)
        val folder = folderRepository.findFolderById(folderId) ?: throw FolderNotFoundException()
        return FolderDto.FolderInfoDto(folder.name, folder.emoji ?: "")
    }
}