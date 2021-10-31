package com.yapp.web2.domain.folder.entity

import com.yapp.web2.domain.BaseTimeEntity
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.PositiveOrZero

@Entity
class Folder(
    var name: String,
    var index: Int,
    var bookmarkCount: Int = 0,

    @ManyToOne
    @JoinColumn(name = "parent_id")
    var parentFolder: Folder?,
) : BaseTimeEntity() {

    companion object {
        fun dtoToEntity(dto: FolderCreateRequest): Folder {
            return dtoToEntity(dto, null)
        }

        fun dtoToEntity(dto: FolderCreateRequest, parentFolder: Folder?): Folder {
            return Folder(
                dto.name, dto.index, 0, parentFolder
            )
        }
    }

    var emoji: String? = null

    @OneToMany(mappedBy = "parentFolder")
    var childrens: MutableList<Folder>? = mutableListOf()

    @OneToMany(mappedBy = "folder")
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

}