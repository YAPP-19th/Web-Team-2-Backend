package com.yapp.web2.domain.folder.service.move.outer

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository

/**
 * 최상위 폴더 -> 상위 폴더
 *  1) 최상위 폴더 리스트에서 현재 index보다 큰 폴더들의 index -1
 *  2) 이동 후 상위 폴더의 children에서 nextIndex 이상인 폴더들의 index + 1
 *  3) 이동 전 최상위 폴더 리스트에서 제거
 *  4) 이동 후 상위 폴더의 children에 추가
 *  5) 이동하는 폴더 index 설정
 */
class FolderMoveFromTopFolderToFolder(
    val request: Folder.FolderMoveRequest,
    private val moveFolder: Folder,
    private val folderRepository: FolderRepository,
    private val user: Account
) : FolderMoveOuterStrategy {

    var topFolderList: MutableList<Folder> = folderRepository.findAllByParentFolderIsNull(user)
    var nextParentFolder = folderRepository.findById(request.nextParentId.toString().toLong()).get()

    override fun indexDecreaseBeforeFolder() {
        topFolderList.stream()
            .filter { it.index > moveFolder.index }
            .forEach { it.index-- }
    }

    override fun indexIncreaseAfterFolder() {
        folderRepository.findByIndexGreaterThanNextFolder(nextParentFolder, request.nextIndex)
            ?.let {
                it.forEach { folder -> folder.index++ }
            }
    }

    override fun removeBeforeFolderList() {
        topFolderList.remove(moveFolder)
    }

    override fun updateFolderAfterMove() {
        nextParentFolder.children?.add(request.nextIndex, moveFolder)
        moveFolder.parentFolder = nextParentFolder
    }

}