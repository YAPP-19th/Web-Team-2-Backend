package com.yapp.web2.domain.folder.service.move.outer

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository

/**
 * 상위 폴더 -> 상위 폴더
 *  1) 상위 폴더 리스트에서 현재 index보다 큰 폴더들의 index -1
 *  2) 이동 후 상위 폴더의 children에서 nextIndex보다 큰 폴더들의 index +1
 *  3) 이동 전 상위 폴더 리스트에서 제거
 *  4) 이동 후 상위 폴더 리스트에 추가
 *  5) 이동하는 폴더 index 설정
 */
class FolderMoveFromFolderToFolder(
    val request: Folder.FolderMoveRequest,
    private val moveFolder: Folder,
    private val folderRepository: FolderRepository,
    private val user: Account
) : FolderMoveOuterStrategy {

    private var prevParentFolder = moveFolder.parentFolder
    private var nextParentFolder = folderRepository.findById(request.nextParentId.toString().toLong()).get()

    override fun indexDecreaseBeforeFolder() {
        folderRepository.findByIndexGreaterThanPrevFolder(prevParentFolder!!, moveFolder.index)
            ?.let {
                it.forEach { folder -> folder.index-- }
            }
    }

    override fun indexIncreaseAfterFolder() {
        folderRepository.findByIndexGreaterThanNextFolder(nextParentFolder, request.nextIndex)
            ?.let {
                it.forEach { folder -> folder.index++ }
            }
    }

    override fun removeBeforeFolderList() {
        prevParentFolder?.children?.remove(moveFolder)
    }

    override fun updateFolderAfterMove() {
        nextParentFolder.children?.add(request.nextIndex, moveFolder)
        moveFolder.parentFolder = nextParentFolder
    }


}