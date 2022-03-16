package com.yapp.web2.domain.bookmark.entity

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.persistence.Id
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Document(collection = "Bookmark")
class Bookmark(
    var userId: Long,
    var folderId: Long?,
    val link: String
) {
    constructor(userId: Long, folderId: Long?, link: String, title: String?) : this(userId, folderId, link) {
        this.title = title
    }

    constructor(userId: Long, folderId: Long?, link: String, title: String?, remindTime: String?) : this(userId, folderId, link, title) {
        this.remindTime = remindTime
    }

    constructor(userId: Long, folderId: Long?, link: String, title: String?, remindTime: String?, image: String?, description: String?) : this(userId, folderId, link, title, remindTime) {
        this.description = description
        this.image = image
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

    @ApiModel(description = "북마크 수정 DTO")
    class UpdateBookmarkDto(
        @ApiModelProperty(value = "북마크 이름", required = true, example = "Change Bookmark")
        @field:NotEmpty(message = "제목을 입력해주세요")
        var title: String,

        @ApiModelProperty(value = "리마인드 여부", example = "true")
        @field:NotNull(message = "리마인드 여부를 입력해주세요")
        var remind: Boolean = false
    )

    @ApiModel(description = "북마크 생성 DTO")
    class AddBookmarkDto(

        // TODO: 2021/12/04 RequestParam 데이터 검증
        @ApiModelProperty(value = "북마크 url", required = true, example = "https://www.naver.com")
        var url: String,

        @ApiModelProperty(value = "북마크 제목", example = "Bookmark Title")
        var title: String?,

        @ApiModelProperty(value = "북마크 리마인드 여부", required = true, example = "true")
        var remind: Boolean,

        @ApiModelProperty(value = "북마크 이미지", example = "https://yapp-bucket-test.s3.ap-northeast-2.amazonaws.com/basicImage.png")
        var image: String?,

        @ApiModelProperty(value = "description", example = "설명 예제...")
        var description: String?
    )

    @ApiModel(description = "북마크 이동 API(폴더별 이동)")
    class MoveBookmarkDto(
        val bookmarkIdList: MutableList<String>,
        @ApiModelProperty(value = "이동 후 폴더 ID", required = true, example = "2")
        val nextFolderId: Long
    )

    @ApiModel(description = "휴지통 복원 API")
    class RestoreBookmarkRequest(

        @ApiModelProperty(value = "복원할 북마크 ID 리스트")
        val bookmarkIdList: MutableList<String>?
    )

    @ApiModel(description = "휴지통 영구삭제 API")
    class TruncateBookmarkRequest(

        @ApiModelProperty(value = "영구삭제할 북마크 ID 리스트")
        val bookmarkIdList: MutableList<String>?
    )

    class RemindList(
        val remindBookmarkList: List<Bookmark>
    )

    class BookmarkIdList(
        val idList: MutableList<String>
    )

    fun restore(): Bookmark {
        this.deleted = false
        this.deleteTime = null
        return this
    }

    fun deletedByFolder() {
        this.folderId = null
        this.folderEmoji = ""
        this.folderName = ""
        this.deleted = true
        this.deleteTime = LocalDateTime.now()
    }

    fun remindOff() {
        this.remindTime = null
    }

    fun updateRemindCheck() {
        this.remindCheck = true
    }

    fun successRemind() {
        this.remindStatus = true
    }
}