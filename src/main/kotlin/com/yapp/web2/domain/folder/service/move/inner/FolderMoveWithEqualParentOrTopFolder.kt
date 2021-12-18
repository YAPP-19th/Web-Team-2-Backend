package com.yapp.web2.domain.folder.service.move.inner

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository

/**
 * (최)상위 폴더 -> (최)상위 폴더(동일한 부모)
 *  1) low index -> high index(ex, 1 -> 3)
 *   1-1) low index 초과, high index 이하 인 최상위 폴더들의 index - 1
 *   1-2) 이동하는 폴더 index 설정
 *  2) high index -> low index(ex, 3 -> 1)
 *   2-1) low index 이상, high index 미만 인 최상위 폴더들의 index - 1
 *   2-2) 이동하는 폴더 index 설정
 */
class FolderMoveWithEqualParentOrTopFolder(
    val request: Folder.FolderMoveRequest,
    private val moveFolder: Folder,
    private val folderRepository: FolderRepository,
    private val user: Account
) : FolderMoveInnerStrategy {

    var folderList: MutableList<Folder> = getFolderList(moveFolder)

    private fun getFolderList(moveFolder: Folder): MutableList<Folder> {
        return when (moveFolder.parentFolder == null) {
            true -> {
                folderRepository.findAllByParentFolderIsNull(user)
            }
            false -> {
                moveFolder.parentFolder?.children!!
            }
        }
    }

    override fun moveFolder() {
        when (isLowIndexToHighIndexMove()) {
            true -> {
                folderList.stream()
                    .filter { it.index > moveFolder.index && it.index <= request.nextIndex }
                    .forEach { it.index-- }
            }
            false -> {
                folderList.stream()
                    .filter { it.index >= request.nextIndex && it.index < moveFolder.index }
                    .forEach { it.index++ }
            }
        }
        moveFolder.index = request.nextIndex
    }

    private fun isLowIndexToHighIndexMove() = moveFolder.index < request.nextIndex
}