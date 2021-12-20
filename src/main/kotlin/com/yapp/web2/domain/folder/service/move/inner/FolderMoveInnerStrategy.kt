package com.yapp.web2.domain.folder.service.move.inner

import com.yapp.web2.domain.folder.entity.Folder

/**
 * 최상위 폴더 -> 최상위 폴더
 * 상위 폴더 -> 상위 폴더(동일한 부모)
 */
interface FolderMoveInnerStrategy {

    fun moveFolder()

    fun updateFolderIndex(moveFolder: Folder, nextIndex: Int) {
        moveFolder.updateIndex(nextIndex)
    }
}