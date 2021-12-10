package com.yapp.web2.domain.folder.service.move.common

import com.yapp.web2.domain.folder.entity.Folder

interface MoveFolderUpdateIndex {

    fun updateFolderIndex(moveFolder: Folder, nextIndex: Int)

}