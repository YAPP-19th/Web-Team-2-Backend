package com.yapp.web2.domain.bookmark.entity

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.folder.entity.Folder
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.persistence.Id

@Document(collection = "Bookmark")
class Bookmark(
    var userId: Long,
    var folderId: Long?,
    val link: String
) {
    constructor(userId: Long, folderId: Long?, link: String, title: String?) : this(userId, folderId, link) {
        this.title = title
    }

    constructor(userId: Long, folderId: Long?, link: String, title: String?, remindTime: String?) : this(
        userId,
        folderId,
        link,
        title
    ) {
        this.remindTime = remindTime
    }

    constructor(
        userId: Long,
        folderId: Long?,
        link: String,
        title: String?,
        image: String?,
        description: String?
    ) : this(userId, folderId, link, title) {
        this.description = description
        this.image = image
    }

    constructor(
        account: Account,
        link: String,
        title: String,
        image: String?,
        description: String,
        remind: Boolean
    ) : this(account.id!!, folderId = null, link, title, image, description) {
        if (remind) remindOn(Remind(account))
    }

    constructor(
        userId: Long,
        folderId: Long?,
        folderEmoji: String?,
        folderName: String?,
        link: String,
        title: String?,
        remindTime: String?,
        image: String?,
        description: String?
    ) : this(userId, folderId, link, title, remindTime) {
        this.folderEmoji = folderEmoji
        this.folderName = folderName
        this.description = description
        this.image = image
    }

    @Id
    lateinit var id: String

    var title: String? = ""
    var folderEmoji: String? = ""
    var folderName: String? = ""

    var clickCount: Int = 0
    var deleteTime: LocalDateTime? = null
    var deleted: Boolean = false
    var description: String? = null
    var image: String? = null

    var saveTime: LocalDateTime = LocalDateTime.now()
    var remindTime: String? = null
    var remindCheck: Boolean = false
    var remindStatus: Boolean = false

    var remindList = mutableListOf<Remind>()

    fun restore(): Bookmark {
        this.deleted = false
        this.deleteTime = null
        return this
    }

    fun updateBookmark(title: String, description: String) {
        this.title = title
        this.description = description
    }

    fun deletedByFolder() {
        this.folderId = null
        this.folderEmoji = ""
        this.folderName = ""
        this.deleted = true
        this.deleteTime = LocalDateTime.now()
    }

    fun deleteBookmark() {
        this.deleted = true
        this.deleteTime = LocalDateTime.now()
    }

    fun changeFolderInfo(folder: Folder) {
        this.folderId = folder.id
        this.folderName = folder.name
        this.folderEmoji = folder.emoji
    }

    fun remindOff(userId: Long) {
        remindList.remove(Remind(userId))
    }

    fun remindOn(remind: Remind) {
        remindList.add(remind)
    }

    fun updateRemindCheck() {
        this.remindCheck = true
    }

    fun moveFolder(nextFolderId: Long) {
        this.folderId = nextFolderId
    }

    fun successRemind() {
        this.remindStatus = true
    }
}