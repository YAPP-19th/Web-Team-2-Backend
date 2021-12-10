package com.yapp.web2.domain.folder.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.yapp.web2.domain.BaseTimeEntity
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.PositiveOrZero

@Entity
class Folder(
    var name: String,
    var index: Int,
    var bookmarkCount: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference
    var parentFolder: Folder?,
) : BaseTimeEntity() {

    companion object {
        fun dtoToEntity(dto: FolderCreateRequest): Folder {
            return dtoToEntity(dto, null)
        }

        fun dtoToEntity(dto: FolderCreateRequest, parentFolder: Folder?): Folder {
            return Folder(dto.name, dto.index, 0, parentFolder)
        }
    }

    var emoji: String? = ""

    @OrderBy("index asc")
    @OneToMany(mappedBy = "parentFolder", cascade = [CascadeType.ALL])
    @JsonManagedReference
    var children: MutableList<Folder>? = mutableListOf()

    @OneToMany(mappedBy = "folder", cascade = [CascadeType.ALL])
    var folders: MutableList<AccountFolder>? = mutableListOf()

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

    fun setParentFolder() {
        this.parentFolder = null
    }

    fun updateFolderToParent(index: Int) {
        this.parentFolder = null
        this.index = index
    }

    fun updateIndex(nextIndex: Int) {
        this.index = nextIndex
    }

    fun updateIndexAndParentFolder(nextIndex: Int, nextParentFolder: Folder) {
        this.index = nextIndex
        this.parentFolder = nextParentFolder
    }

    @ApiModel(description = "폴더 생성 DTO")
    class FolderCreateRequest(
        @ApiModelProperty(value = "부모 폴더 ID(0일경우 부모폴더)", example = "0")
        @field: PositiveOrZero
        val parentId: Long = 0,

        @ApiModelProperty(value = "폴더 이름", required = true, example = "부모 폴더")
        @field: NotEmpty(message = "폴더명을 입력해주세요")
        val name: String,

        @ApiModelProperty(value = "폴더 순서", required = true, example = "2")
        @field: PositiveOrZero
        val index: Int = 1
    )

    @ApiModel(description = "폴더 생성 Response")
    class FolderCreateResponse(
        @ApiModelProperty(value = "폴더 ID", example = "3")
        val folderId: Long
    )

    @ApiModel(description = "폴더 이름 수정 DTO")
    class FolderNameChangeRequest(
        @ApiModelProperty(value = "수정할 폴더 이름", required = true, example = "Change Folder")
        @field: NotEmpty(message = "변경할 폴더 명을 입력해주세요")
        val name: String
    )

    @ApiModel(description = "폴더 이모지 수정 DTO")
    class FolderEmojiChangeRequest(
        @ApiModelProperty(value = "수정할 이모지 이름", required = true, example = "U+1F604")
        @field: NotEmpty(message = "변경할 이모지를 입력해주세요")
        val emoji: String
    )

    @ApiModel(description = "폴더 이동(드래그 & 드랍) DTO")
    class FolderMoveRequest(
        @ApiModelProperty(value = "이동 후 부모폴더 ID(\"root\" => 최상위 부모폴더)", required = true, example = "2")
        var nextParentId: Any,

        @ApiModelProperty(value = "이동 후 폴더 Index", required = true, example = "2")
        @field: PositiveOrZero
        val nextIndex: Int
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
            var data: RootFolderData
        )

        class RootFolderData(
            var name: String,
            var emoji: String
        )
    }

}
