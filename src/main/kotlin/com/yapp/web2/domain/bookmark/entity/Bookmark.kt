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

    var remindList = mutableListOf<Remind>()

    constructor(userId: Long, folderId: Long?, link: String, title: String?) : this(userId, folderId, link) {
        this.title = title
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

    fun remindOff() {
        remindList = mutableListOf()
    }

    fun isRemindExist(remind: Remind): Boolean {
        return remindList.contains(remind)
    }

    fun remindOn(remind: Remind) {
        remindList.add(remind)
    }

    fun moveFolder(nextFolderId: Long) {
        this.folderId = nextFolderId
    }

    fun hitClickCount() {
        this.clickCount++
    }

    override fun toString(): String {
        return "Bookmark(userId=$userId, link='$link', id='$id', title=$title, remindList=$remindList)"
    }

}