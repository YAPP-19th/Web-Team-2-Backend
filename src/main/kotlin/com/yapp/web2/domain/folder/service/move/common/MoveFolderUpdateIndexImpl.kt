package com.yapp.web2.domain.folder.service.move.common

import com.yapp.web2.domain.folder.entity.Folder

class MoveFolderUpdateIndexImpl : MoveFolderUpdateIndex {

    override fun updateFolderIndex(moveFolder: Folder, nextIndex: Int) {
        moveFolder.updateIndex(nextIndex)
    }

}