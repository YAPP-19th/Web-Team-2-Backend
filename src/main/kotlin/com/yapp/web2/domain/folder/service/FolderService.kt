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

                return folder
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

    /**
     * 폴더 이동(드래그 앤 드랍) 로직
     * 1) 이동 전 부모폴더 & 폴더리스트, 이동 후 부모폴더 & 폴더리스트, 이동 폴더 조회
     * 2) 이동 전 폴더 리스트에서 자신보다 index가 큰 폴더들은 각각 -1
     * 3) 이동 후 폴더 리스트에서 자신보다 index가 큰 폴더들은 각각 +1
     * 4) 이동 전 폴더 리스트에서 제거 & 이동 후 폴더 리스트에 추가
     * 5) 이동 전, 이동 후 폴더 리스트 초기화 & DB 저장
     */
    @Transactional
    fun moveFolder(id: Long, request: Folder.FolderMoveRequest) {
        // 1)
        val prevParentFolder = folderRepository.findByIdOrNull(request.prevParentId)
        val nextParentFolder = folderRepository.findByIdOrNull(request.nextParentId)
        val prevChildFolderList = prevParentFolder?.childrens
        val nextChildFolderList = nextParentFolder?.childrens
        val moveFolder = folderRepository.findByIdOrNull(id)

        // 2)
        folderRepository.findByIndexGreaterThan(prevParentFolder!!, request.prevIndex)
            ?.let {
                it.forEach { folder -> folder.index-- }
            }

        // 3)
        folderRepository.findByIndexGreaterThan(nextParentFolder!!, request.nextIndex)
            ?.let {
                it.forEach { folder -> folder.index++ }
            }

        // 4)
        prevChildFolderList?.removeAt(request.prevIndex)
        moveFolder?.let { nextChildFolderList?.add(request.nextIndex, it) }

        // 5)
        prevParentFolder.childrens = prevChildFolderList
        nextParentFolder.childrens = nextChildFolderList
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