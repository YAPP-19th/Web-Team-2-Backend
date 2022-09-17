package com.yapp.web2.domain.bookmark.repository

import com.yapp.web2.domain.bookmark.entity.Bookmark
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface BookmarkRepository : MongoRepository<Bookmark, String>, MongoTemplateRepository {
    fun findBookmarkById(id: String): Bookmark?

    fun findAllByFolderId(folderId: Long): List<Bookmark>

    fun findAllByFolderId(folderIdList: List<Long?>): List<Bookmark>

    fun findAllByFolderIdAndDeletedIsFalse(folderId: Long, pageable: Pageable): Page<Bookmark>

    fun findAllByUserIdAndDeletedIsTrue(userId: Long, pageable: Pageable): Page<Bookmark>

    @Query("{\$and: [{'userId': ?2} , {\$or: [{'title': {\$regex: \".*?0.*\"}}, {\"link\": {\$regex: \".*?1.*\"}}]}]}")
    fun findByTitleContainingIgnoreCaseOrLinkContainingIgnoreCaseAndUserId(title: String, link: String, userId: Long, pageable: Pageable): Page<Bookmark>

    fun findByFolderId(id: Long): List<Bookmark>

    @Query(value = "{remindList: {\$elemMatch: {remindTime: ?0}}}")
    fun findAllBookmarkByRemindTime(today: String): MutableList<Bookmark>

    fun findAllByDeletedIsTrueAndDeleteTimeBefore(time: LocalDateTime): List<Bookmark>

    @Query(value = "{ 'remindList': { \$elemMatch: { 'fcmToken' : ?0 } } }")
    fun findAllBookmarkByFcmToken(fcmToken: String): List<Bookmark>

    @Query(value = "{\$and: [{folderId: {\$in: ?1}}, {deleted: false}, {remindList: {\$elemMatch: {userId : ?0}}}]}")
    fun findRemindBookmarkInFolder(userId: Long, folderIdList: List<Long>, pageable: Pageable): Page<Bookmark>

    @Query(value = "{\$or: [{folderId: {\$in: ?1}}, {userId: ?0}]}")
    fun findAllBookmark(userId: Long, folderIdList: List<Long>, pageable: Pageable): Page<Bookmark>

    @Query(value = "{\$and: [{remindList: {\$elemMatch: {userId: ?0}}}, {remindList: {\$elemMatch: {remindTime: ?1}}}]}")
    fun findAllTodayRemindBookmarksByUserId(userId: Long, today: String): List<Bookmark>

    @Query(value = "{ 'remindList': { \$elemMatch: { 'userId' : ?0 } } }")
    fun findRemindBookmark(userId: Long, pageable: Pageable): Page<Bookmark>

    @Query(value = "{ 'remindList': { \$elemMatch: { 'userId' : ?0 }}}")
    fun findAllBookmarkByUserIdInRemindList(userId: Long): MutableList<Bookmark>
}