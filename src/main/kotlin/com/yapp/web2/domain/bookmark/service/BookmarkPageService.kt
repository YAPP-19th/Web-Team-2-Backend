package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.ObjectNotFoundException
import com.yapp.web2.security.jwt.JwtProvider
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BookmarkPageService(
    private val bookmarkRepository: BookmarkRepository,
    private val folderRepository: FolderRepository,
    private val jwtProvider: JwtProvider
) {
    fun getAllPageByFolderId(folderId: Long, pageable: Pageable, remind: Boolean): Page<Bookmark> {
        checkFolderAbsence(folderId)
        return when (remind) {
            true -> bookmarkRepository.findAllByFolderIdAndRemindTimeIsNotNull(folderId, pageable)
            false -> bookmarkRepository.findAllByFolderId(folderId, pageable)
        }
    }


    fun getAllPageByUserId(token:String, pageable: Pageable, remind: Boolean): Page<Bookmark> {
        val idFromToken = jwtProvider.getIdFromToken(token)
        return when (remind) {
            true -> bookmarkRepository.findAllByUserIdAndDeleteTimeIsNotNullAndRemindTimeIsNotNull(idFromToken, pageable)
            false -> bookmarkRepository.findAllByUserIdAndDeleteTimeIsNotNull(idFromToken, pageable)
        }
    }

    private fun checkFolderAbsence(folderId: Long) {
        val folder = folderRepository.findById(folderId)
        if (folder.isEmpty) throw ObjectNotFoundException("해당 폴더가 존재하지 않습니다.")
    }
}