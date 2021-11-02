package com.yapp.web2.domain.bookmark.repository

import com.yapp.web2.domain.bookmark.entity.Bookmark
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BookmarkRepository : MongoRepository<Bookmark, Long> {
    fun findAllByFolderIdAndRemindTimeIsNotNull(FolderId: Long, pageable: Pageable): Page<Bookmark>
    fun findAllByFolderId(FolderId: Long, pageable: Pageable): Page<Bookmark>
    fun findAllByFolderId(FolderId: Long): List<Bookmark>
}