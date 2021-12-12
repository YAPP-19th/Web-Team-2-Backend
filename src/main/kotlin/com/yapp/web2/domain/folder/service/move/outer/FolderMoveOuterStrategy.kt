package com.yapp.web2.domain.folder.service.move.outer

import com.yapp.web2.domain.folder.entity.Folder

/**
 * 최상위 폴더 -> 상위 폴더
 * 상위 폴더 -> 최상위 폴더
 * 상위 폴더 -> 상위 폴더
 */
interface FolderMoveOuterStrategy {

    fun indexDecreaseBeforeFolder()

    fun indexIncreaseAfterFolder()

    fun removeBeforeFolderList()

    fun updateFolderAfterMove()

    fun updateFolderIndex(moveFolder: Folder, nextIndex: Int) {
        moveFolder.updateIndex(nextIndex)
    }

    fun folderDragAndDrop(moveFolder: Folder, nextIndex: Int) {
        indexDecreaseBeforeFolder()
        indexIncreaseAfterFolder()
        removeBeforeFolderList()
        updateFolderAfterMove()
        updateFolderIndex(moveFolder, nextIndex)
    }

}