package com.yapp.web2.domain.bookmark.entity

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Document(collection = "Bookmark")
class Bookmark(
    var userId: Long,
    var folderId: Long,
    var information: Information
) {
    @Id
    lateinit var id: ObjectId

    class UpdateBookmarkDto(
        var title: String?,
        var remind: Boolean?
    )
}