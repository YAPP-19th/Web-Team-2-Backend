package com.yapp.web2.domain.bookmark.entity

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Document(collection = "Bookmark")
class Bookmark(
    var userId: Long,
    var folderId: Long,
    val link: String
) {
    @Id
    lateinit var id: ObjectId

    var title: String? = ""

    var remindTime: LocalDateTime? = null
    var clickCount: Int = 0
    var deleteTime: LocalDateTime? = null
    var deleted: Boolean = false

    val saveTime: LocalDateTime = LocalDateTime.now()

    constructor(userId: Long, folderId: Long, link: String, title: String?) : this(userId, folderId, link) {
        this.title = title
    }

    constructor(userId: Long, folderId: Long, link: String, title: String?, remindTime: LocalDateTime) : this(userId, folderId, link, title) {
        this.remindTime = remindTime
    }

    constructor(userId: Long, folderId: Long, link: String, remindTime: LocalDateTime) : this(userId, folderId, link) {
        this.remindTime = remindTime
    }

    class UpdateBookmarkDto(
        var title: String?,
        var remind: Boolean?
    )

    class AddBookmarkDto(
        var url: String,
        var title: String?,
        var remind: Boolean
    )
}