package com.yapp.web2.domain.bookmark.entity

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.folder.entity.Folder
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "bookmarkInterface")
abstract class BookmarkInterface() {
    companion object {
        fun copyBookmark(account: Account, bookmark: BookmarkInterface): PersonalBookmark {
            return PersonalBookmark(
                account,
                bookmark.link,
                bookmark.title,
                bookmark.image,
                bookmark.description,
                bookmark.id
            )
        }
    }

    @Id
    var id: String = ""
    var userId: Long? = null
    var link: String = ""
    var title: String = ""
    var description: String = ""
    var image: String? = null

    var folderId: Long? = null

    // TODO: 2022/05/03 이거 그냥 nullable 처리 안하고 ""로 진행했을 경우의 문제점은?
    var folderEmoji: String? = null
    var folderName: String = ""
    var clickCount: Int = 0

    var deleteTime: LocalDateTime? = null
    var deleted: Boolean = false

    var saveTime: LocalDateTime = LocalDateTime.now()

    // TODO: 2022/05/14 copy 북마크 아이디
    var parentBookmarkId: String? = null

    constructor(
        account: Account,
        link: String,
        title: String,
        image: String?,
        description: String
    ) : this() {
        this.userId = account.id
        this.link = link
        this.title = title
        this.image = image
        this.description = description
    }

    abstract fun moveFolder(nextFolderId: Long)
    abstract fun remindOn(remindElement: Long)
    abstract fun remindOff(remindElement: Long)
    abstract fun remindOff()

    fun changeFolderInfo(folder: Folder) {
        this.folderId = folder.id
        this.folderName = folder.name
        this.folderEmoji = folder.emoji
    }

    fun deleteBookmark() {
        this.deleted = true
        this.deleteTime = LocalDateTime.now()
    }

    fun updateBookmark(title: String, description: String) {
        this.title = title
        this.description = description
    }

    fun changeFolderInfo(folderId: Long?, folderEmoji: String?, folderName: String) {
        this.folderId = folderId
        this.folderEmoji = folderEmoji
        this.folderName = folderName
    }
}