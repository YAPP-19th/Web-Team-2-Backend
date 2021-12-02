package com.yapp.web2.domain.bookmark.entity

import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.persistence.Id
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Document(collection = "Bookmark")
class Bookmark(
    var userId: Long,
    var folderId: Long?,
    val link: String
) {
    @Id
    lateinit var id: String

    var title: String? = ""

    var remindTime: LocalDateTime? = null
    var clickCount: Int = 0
    var deleteTime: LocalDateTime? = null
    var deleted: Boolean = false
    var description: String? = null

    var saveTime: LocalDateTime = LocalDateTime.now()

    constructor(userId: Long, folderId: Long, link: String, title: String?) : this(userId, folderId, link) {
        this.title = title
    }

    constructor(userId: Long, folderId: Long, link: String, remindTime: LocalDateTime) : this(userId, folderId, link) {
        this.remindTime = remindTime
    }

    constructor(userId: Long, folderId: Long, link: String, title: String?, remindTime: LocalDateTime?) : this(userId, folderId, link, title) {
        this.remindTime = remindTime
    }

    constructor(userId: Long, folderId: Long, description: String?, link: String, title: String?, remindTime: LocalDateTime?) : this(userId, folderId, link, title, remindTime) {
        this.description = description
    }

    data class UpdateBookmarkDto(
        @field:NotEmpty(message = "제목을 입력해주세요")
        var title: String,
        @field:NotNull(message = "리마인드 여부를 입력해주세요")
        var remind: Boolean
    )

    class AddBookmarkDto(
        var url: String,
        var title: String?,
        var description: String?,
        var remind: Boolean
    )

    class MoveBookmarkDto(
        val prevFolderId: Long,
        val nextFolderId: Long
    )


    class RestoreBookmarkRequest(
        val bookmarkIdList: MutableList<String>?
    )

    class TruncateBookmarkRequest(
        val bookmarkIdList: MutableList<String>?
    )

    fun restore() {
        this.deleted = false
        this.deleteTime = null
    }

    fun deletedByFolder() {
        this.folderId = null
        this.deleted = true
    }

}