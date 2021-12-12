package com.yapp.web2.domain.folder.service.move.factory

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.domain.folder.service.move.outer.FolderMoveFromFolderToFolder
import com.yapp.web2.domain.folder.service.move.outer.FolderMoveFromFolderToTopFolder
import com.yapp.web2.domain.folder.service.move.outer.FolderMoveFromTopFolderToFolder
import com.yapp.web2.domain.folder.service.move.outer.FolderMoveOuterStrategy

class FolderMoveFactory {

    companion object {
        fun getFolderMove(
            request: Folder.FolderMoveRequest,
            moveFolder: Folder,
            folderRepository: FolderRepository,
            user: Account
        ): FolderMoveOuterStrategy {
            val nextParentId = request.nextParentId.toString().toLongOrNull()

            /* 최상위 -> 상위 */
            if (moveFolder.parentFolder == null && nextParentId != null) {
                return FolderMoveFromTopFolderToFolder(request, moveFolder, folderRepository, user)
            }

            /* 상위 -> 최상위 */
            if (moveFolder.parentFolder != null && nextParentId == null) {
                return FolderMoveFromFolderToTopFolder(request, moveFolder, folderRepository, user)
            }

            /* 상위 -> 상위 */
            return FolderMoveFromFolderToFolder(request, moveFolder, folderRepository, user)

        }
    }

}