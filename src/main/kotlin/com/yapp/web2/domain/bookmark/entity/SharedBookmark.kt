package com.yapp.web2.domain.bookmark.entity


import java.time.LocalDateTime
import javax.persistence.Id

class SharedBookmark(var rootFolderId: Long) : BookmarkInterface {
    @Id
    override var id: String = ""
    override var userId: Long? = null
    override var link: String = ""
    override var title: String = ""
    override var description: String = ""
    override var image: String? = null

    override var folderId: Long? = null
    override var folderEmoji: String? = ""
    override var folderName: String = ""
    override var clickCount: Int = 0

    override var deleteTime: LocalDateTime? = null
    override var deleted: Boolean = false

    override var saveTime: LocalDateTime = LocalDateTime.now()
    var remindAccountList = mutableListOf<Long>()

    constructor(bookmark: BookmarkInterface, rootFolderId: Long): this(rootFolderId) {
        this.userId = bookmark.userId
        this.link = bookmark.link
        this.title = bookmark.title
        this.description = bookmark.description
        this.image = bookmark.image
        this.folderId = bookmark.folderId
        this.folderEmoji = bookmark.folderEmoji
        this.folderName = bookmark.folderName
    }


    override fun moveFolder(nextFolderId: Long) {
        this.folderId = nextFolderId
    }

    override fun remindOn(remindElement: Long) {
        this.remindAccountList.add(remindElement)
    }

    override fun remindOff(remindElement: Long) {
        this.remindAccountList.remove(remindElement)
    }

    override fun remindOff() {
        throw RuntimeException()
    }
}