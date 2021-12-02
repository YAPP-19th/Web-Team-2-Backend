package com.yapp.web2.domain.folder.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.yapp.web2.domain.BaseTimeEntity
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
    @OneToMany(mappedBy = "parentFolder")
    @JsonManagedReference
    var children: MutableList<Folder>? = mutableListOf()

    @OneToMany(mappedBy = "folder", cascade = [CascadeType.PERSIST])
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

    fun updateIndexAndParentFolder(nextIndex: Int, nextParentFolder: Folder) {
        this.index = nextIndex
        this.parentFolder = nextParentFolder
    }

    class FolderCreateRequest(
        @field: PositiveOrZero
        val parentId: Long = 0,

        @field: NotEmpty(message = "폴더명을 입력해주세요")
        val name: String,

        @field: PositiveOrZero
        val index: Int = 1
    )

    class FolderNameChangeRequest(
        val name: String
    )

    class FolderEmojiChangeRequest(
        val emoji: String
    )

    class FolderMoveRequest(
        @field: PositiveOrZero
        val prevParentId: Long,

        @field: PositiveOrZero
        val nextParentId: Long,

        @field: PositiveOrZero
        val prevIndex: Int,

        @field: PositiveOrZero
        val nextIndex: Int
    )

    class FolderReadResponse(
        var rootId: Long,
        var items: FolderItem
    ) {

        class FolderItem(
            var root: Root,
            var folder: MutableList<RootFolder> = mutableListOf()
        )

        class Root(
            var id: Long, // 유저 ID
            var rootFolders: MutableList<Long> = mutableListOf()
        )

        class RootFolder(
            var id: Long, // 폴더 ID
            var children: MutableList<Int>? = mutableListOf(),
            var data: RootFolderData
        )

        class RootFolderData(
            var name: String,
            var emoji: String
        )
    }

}