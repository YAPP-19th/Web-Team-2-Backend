

package com.yapp.web2.domain.bookmark.repository

import com.yapp.web2.domain.bookmark.entity.Bookmark
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface BookmarkRepository : MongoRepository<Bookmark, String> {
    fun findBookmarkById(id: String): Bookmark?

    fun findAllByFolderId(folderId: Long): List<Bookmark>

    fun findAllByFolderIdAndDeleteTimeIsNullAndRemindTimeIsNotNull(folderId: Long, pageable: Pageable): Page<Bookmark>

    fun findAllByFolderIdAndDeleteTimeIsNull(folderId: Long, pageable: Pageable): Page<Bookmark>

    fun findAllByUserIdAndRemindTimeIsNotNullAndDeleteTimeIsNull(userId: Long, pageable: Pageable): Page<Bookmark>

    fun findAllByUserIdAndDeleteTimeIsNull(userId: Long, pageable: Pageable): Page<Bookmark>

    fun findAllByUserIdAndDeleteTimeIsNotNullAndRemindTimeIsNotNull(userId: Long, pageable: Pageable): Page<Bookmark>

    fun findAllByUserIdAndDeleteTimeIsNotNull(userId: Long, pageable: Pageable): Page<Bookmark>

    @Query("{\$and: [{'userId': ?2} , {\$or: [{'title': {\$regex: \".*?0.*\"}}, {\"link\": {\$regex: \".*?1.*\"}}]}]}")
    fun findByTitleContainingIgnoreCaseOrLinkContainingIgnoreCaseAndUserId(title: String, link: String, userId: Long, pageable: Pageable): Page<Bookmark>

    fun findByFolderId(id: Long): List<Bookmark>

    fun findAllByRemindTimeAndDeleteTimeIsNull(remindTime: LocalDate): List<Bookmark>

    fun findAllByRemindTimeAfterAndUserIdAndDeleteTimeIsNull(now: LocalDate, userId: Long): List<Bookmark>

    fun findAllByUserId(userId: Long): List<Bookmark>

    fun findAllByUserIdAndRemindCheckIsFalseAndRemindStatusIsTrue(userId: Long): List<Bookmark>
}