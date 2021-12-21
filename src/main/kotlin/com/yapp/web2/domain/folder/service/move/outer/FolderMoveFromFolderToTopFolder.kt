package com.yapp.web2.domain.folder.service.move.outer

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository

/**
 * 상위 폴더 -> 최상위 폴더
 *  1) 상위 폴더 리스트에서 현재 index보다 큰 폴더들의 index -1
 *  2) 이동 후 최상위 폴더 리스트에서 nextIndex 이상인 폴더들의 index +1
 *  3) 이동 전 상위 폴더 리스트에서 제거
 *  4) 이동 후 최상위 폴더로 설정(부모 폴더 Null)
 *  5) 이동하는 폴더 index 설정
 */
class FolderMoveFromFolderToTopFolder(
    val request: Folder.FolderMoveRequest,
    private val moveFolder: Folder,
    private val folderRepository: FolderRepository,
    private val user: Account
) : FolderMoveOuterStrategy {

    var topFolderList: MutableList<Folder> = folderRepository.findAllByParentFolderIsNull(user)
    var prevParentFolder = moveFolder.parentFolder

    override fun indexDecreaseBeforeFolder() {
        folderRepository.findByIndexGreaterThanPrevFolder(prevParentFolder!!, moveFolder.index)
            ?.let {
                it.forEach { folder -> folder.index-- }
            }
    }

    override fun indexIncreaseAfterFolder() {
        topFolderList.stream()
            .filter { it.index >= request.nextIndex }
            .forEach { it.index++ }
    }

    override fun removeBeforeFolderList() {
        prevParentFolder?.children?.remove(moveFolder)
    }

    override fun updateFolderAfterMove() {
        moveFolder.setTopFolder()
        moveFolder.index = request.nextIndex
    }
}