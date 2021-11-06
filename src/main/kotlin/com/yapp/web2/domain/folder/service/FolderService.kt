package com.yapp.web2.domain.folder.service

import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.custom.FolderNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class FolderService(
    private val folderRepository: FolderRepository,
    private val bookmarkRepository: BookmarkRepository
) {
    companion object {
        private val folderNotFoundException = FolderNotFoundException()
    }

    @Transactional
    fun createFolder(request: Folder.FolderCreateRequest): Folder {
        return when (isParentFolder(request.parentId)) {
            true -> {
                val rootFolder = Folder.dtoToEntity(request)
                folderRepository.save(rootFolder)
            }
            false -> {
                val parentFolder = folderRepository.findById(request.parentId).get()
                val childrenFolderList = parentFolder.childrens
                val folder = Folder.dtoToEntity(request, parentFolder)
                childrenFolderList?.add(folder)

                // TODO: parentFolder도 update 해야하지않나 ?
                folderRepository.save(folder)
            }
        }
    }

    private fun isParentFolder(parentId: Long) = parentId == 0L

    @Transactional
    fun changeFolderName(id: Long, request: Folder.FolderNameChangeRequest): String {
        folderRepository.findByIdOrNull(id)
            ?.let { folder ->
                folder.name = request.name
                folderRepository.save(folder)
                return request.name
            }
            ?: throw folderNotFoundException
    }

    @Transactional
    fun moveFolder(id: Long, request: Folder.FolderMoveRequest) {
        val prevParentFolder = folderRepository.findByIdOrNull(request.prevParentId)
        val nextParentFolder = folderRepository.findByIdOrNull(request.nextParentId)
        val prevChildrenFolderList = prevParentFolder?.childrens
        val nextChildFolderList = nextParentFolder?.childrens
        val moveFolder = folderRepository.findByIdOrNull(id)

        prevChildrenFolderList?.removeAt(request.prevIndex)
        moveFolder?.let { nextChildFolderList?.add(request.nextIndex, it) }
    }

    @Transactional
    fun deleteAllBookmark(id: Long) {
        bookmarkRepository.findByFolderId(id)
            ?.let { list ->
                list.forEach { bookmarkRepository.delete(it) }
            }
    }

    @Transactional
    fun deleteFolder(id: Long) {
        folderRepository.deleteById(id)
    }
}