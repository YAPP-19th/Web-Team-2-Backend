package com.yapp.web2.domain.bookmark.entity

import com.yapp.web2.domain.folder.entity.Folder
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collation = "Bookmark")
interface BookmarkInterface {

    var id: String
    var userId: Long?
    var link: String
    var title: String
    var description: String
    var image: String?

    var folderId: Long?
    // TODO: 2022/05/03 이거 그냥 nullable 처리 안하고 ""로 진행했을 경우의 문제점은?
    var folderEmoji: String?
    var folderName: String
    var clickCount: Int

    var deleteTime: LocalDateTime?
    var deleted: Boolean

    var saveTime: LocalDateTime

    fun moveFolder(nextFolderId: Long)
    fun remindOn(remindElement: Long)
    fun remindOff(remindElement: Long)
    fun remindOff()
    fun changeFolderInfo(folder: Folder) {
        this.folderId = folder.id
        this.folderName = folder.name
        this.folderEmoji = folder.emoji
    }
}