package com.yapp.web2.domain.folder.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.yapp.web2.domain.BaseTimeEntity
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.springframework.transaction.annotation.Transactional
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.PositiveOrZero

@Entity
class Folder(
    @Column
    var name: String,

    @Column(nullable = false)
    var index: Int,

    @Column(nullable = false)
    var bookmarkCount: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference
    var parentFolder: Folder?,
) : BaseTimeEntity() {

    @Column
    var emoji: String? = ""

    @OrderBy("index asc")
    @OneToMany(mappedBy = "parentFolder", cascade = [CascadeType.ALL])
    @JsonManagedReference
    var children: MutableList<Folder>? = mutableListOf()

    @OneToMany(mappedBy = "folder", cascade = [CascadeType.ALL])
    var folders: MutableList<AccountFolder>? = mutableListOf()

    // TODO: 2022/05/06 공유 북마크인지 확인하기 위해서 추가한 컬럼
    var share: Boolean? = false

    var rootFolderId: Long? = null

    companion object {
        fun dtoToEntity(dto: FolderCreateRequest, index: Int): Folder {
            return dtoToEntity(dto, null, index)
        }

        fun dtoToEntity(dto: FolderCreateRequest, parentFolder: Folder?, index: Int): Folder {
            return Folder(dto.name, index, 0, parentFolder)
        }
    }

    @ApiModel(description = "폴더 생성 DTO")
    class FolderCreateRequest(
        @ApiModelProperty(value = "부모 폴더 ID(0일경우 부모폴더)", example = "0")
        @field: PositiveOrZero
        val parentId: Long = 0,

        @ApiModelProperty(value = "폴더 이름", required = true, example = "부모 폴더")
        @field: NotEmpty(message = "폴더명을 입력해주세요")
        val name: String
    )

    @ApiModel(description = "폴더 생성 Response")
    class FolderCreateResponse(
        @ApiModelProperty(value = "폴더 ID", example = "3")
        val folderId: Long
    )

    @ApiModel(description = "폴더 수정 DTO")
    class FolderChangeRequest(
        @ApiModelProperty(value = "수정할 폴더 이모지", example = "U+1F604")
        val emoji: String?,

        @ApiModelProperty(value = "수정할 폴더 이름", example = "폴더 이름 수정")
        val name: String?
    )

    @ApiModel(description = "폴더 이동(드래그 & 드랍) DTO")
    class FolderMoveRequest(
        @ApiModelProperty(value = "이동 후 부모폴더 ID(\"root\" => 최상위 부모폴더)", required = true, example = "2")
        var nextParentId: Any,

        @ApiModelProperty(value = "이동 후 폴더 Index", required = true, example = "2")
        @field: PositiveOrZero
        val nextIndex: Int
    )

    @ApiModel(description = "폴더 이동(버튼) DTO")
    class FolderMoveButtonRequest(
        @ApiModelProperty(value = "이동할 폴더들의 ID 리스트", required = true)
        val folderIdList: MutableList<Long>,

        @ApiModelProperty(value = "이동하는 폴더의 ID", required = true)
        val nextFolderId: Long
    )

    @ApiModel(description = "폴더 리스트 삭제 DTO")
    class FolderListDeleteRequest(
        @ApiModelProperty(value = "삭제할 폴더들의 ID 리스트", required = true, example = "[1,4,5]")
        val deleteFolderIdList: MutableList<Long> = mutableListOf()
    )

    class FolderFindAllResponse(
        var rootId: Long,
        var items: FolderItem
    ) {

        class FolderItem(
            var root: Root,
            var folder: MutableMap<String, Any> = mutableMapOf()
        )

        class Root(
            //var id: Long, // 유저 ID
            val id: String = "root",
            var children: MutableList<Long> = mutableListOf()
        )

        class RootFolder(
            var id: Long, // 폴더 ID
            var children: MutableList<Long>? = mutableListOf(),
            var data: RootFolderData,
            val isExpanded: Boolean = false
        )

        class RootFolderData(
            var name: String,
            var emoji: String
        )
    }

    @ApiModel(description = "자식 폴더 리스트 & 부모 폴더 리스트 조회 DTO")
    class FolderListResponse(
        @ApiModelProperty(value = "자식 or 부모 폴더 ID", required = true, example = "12")
        var folderId: Long,

        @ApiModelProperty(value = "이모지", required = true, example = "🥕")
        val emoji: String,

        @ApiModelProperty(value = "자식 or 부모 폴더 이름", required = true, example = "자식 폴더 1-2")
        var name: String
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Folder
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Folder(name='$name', index=$index, bookmarkCount=$bookmarkCount, parentFolder=$parentFolder, emoji=$emoji)"
    }

    fun setTopFolder() {
        this.parentFolder = null
    }

    fun updateIndex(nextIndex: Int) {
        this.index = nextIndex
    }

    @Transactional
    fun updateBookmarkCount(count: Int) {
        this.bookmarkCount += count
    }

    fun setRootFolder(folder: Folder) {
        folder.rootFolderId?.let {
            this.rootFolderId = it
        } ?: let { this.rootFolderId = folder.id }
    }
}