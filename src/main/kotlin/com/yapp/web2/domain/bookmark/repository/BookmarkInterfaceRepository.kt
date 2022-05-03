package com.yapp.web2.domain.bookmark.repository

import com.yapp.web2.domain.bookmark.entity.BookmarkInterface
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BookmarkInterfaceRepository : MongoRepository<BookmarkInterface, String>{
    fun findBookmarkInterfaceById(id: String): BookmarkInterface?
    fun deleteBookmarkInterfacesByParentBookmarkIdAndUserId(parentBookmarkId: String, userId: Long)
}