package com.yapp.web2.domain.bookmark.repository

import com.yapp.web2.domain.bookmark.entity.BookmarkInterface
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BookmarkInterfaceRepository : MongoRepository<BookmarkInterface, String>{
    fun findBookmarkInterfaceById(id: String): BookmarkInterface?
    fun deleteAllByParentBookmarkId(parentBookmarkIdList: List<String>)
    fun deleteByParentBookmarkIdAndUserId(parentBookmarkId: String, userId: Long)
    fun findAllByParentBookmarkId(bookmarkId: String): List<BookmarkInterface>
    fun findAllByFolderId(folderId: Long): List<BookmarkInterface>
    fun findAllByFolderId(folderId: Long, pageable: Pageable): Page<BookmarkInterface>?
    fun findAllByUserId(userId: Long): List<BookmarkInterface>

    // page 조회
    fun findAllByFolderIdAndDeleteTimeIsNull(folderId: Long): List<BookmarkInterface>
    fun findAllByUserIdAndDeleteTimeIsNotNull(userId: Long): List<BookmarkInterface>
    fun findAllByUserIdAndDeleteTimeIsNull(userId: Long): List<BookmarkInterface>
}