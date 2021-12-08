package com.yapp.web2.domain.bookmark.repository

import com.yapp.web2.domain.bookmark.entity.Bookmark
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface BookmarkRepository : MongoRepository<Bookmark, String> {
    fun findAllByFolderId(folderId: Long): List<Bookmark>
    fun findAllByFolderIdAndRemindTimeIsNotNull(folderId: Long, pageable: Pageable): Page<Bookmark>
    fun findAllByFolderId(folderId: Long, pageable: Pageable): Page<Bookmark>
    fun findAllByUserIdAndDeleteTimeIsNotNullAndRemindTimeIsNotNull(userId: Long, pageable: Pageable): Page<Bookmark>
    fun findAllByUserIdAndDeleteTimeIsNotNull(userId: Long, pageable: Pageable): Page<Bookmark>
    fun findByUserIdAndTitleContainingIgnoreCaseOrLinkContainingIgnoreCase(userId: Long, title: String, link: String, pageable: Pageable): Page<Bookmark>
    fun findByFolderId(id: Long): List<Bookmark>
    fun findAllByRemindTimeAndDeleteTimeIsNull(remindTime: LocalDate): List<Bookmark>
}