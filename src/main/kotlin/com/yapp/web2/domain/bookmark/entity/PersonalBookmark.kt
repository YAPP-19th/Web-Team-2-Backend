package com.yapp.web2.domain.bookmark.entity

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Id

class PersonalBookmark : BookmarkInterface {

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
    var parentBookmarkId: String? = null
    var remindTime: String? = null
    var remindCheck: Boolean = false
    var remindStatus: Boolean = false

    override fun moveFolder(nextFolderId: Long) {
        this.folderId = nextFolderId
    }

    override fun remindOn(remindElement: Long) {
        this.remindTime = LocalDate.now().plusDays(remindElement).toString()
    }

    override fun remindOff(remindElement: Long) {
        throw RuntimeException()
    }

    override fun remindOff() {
        this.remindTime = null
    }
}