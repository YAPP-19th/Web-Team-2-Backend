package com.yapp.web2.domain.bookmark.repository

import com.yapp.web2.domain.bookmark.entity.Bookmark
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BookmarkRepository : MongoRepository<Bookmark, Long> {

    fun findByFolderId(id: Long): MutableList<Bookmark>?

}