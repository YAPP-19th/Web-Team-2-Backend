package com.yapp.web2.domain.folder.entity

import com.yapp.web2.domain.BaseTimeEntity
import com.yapp.web2.domain.user.entity.Account
import javax.persistence.*

@Entity
class Folder(
    var name: String,
    var emoji: String?,
    var bookmarkCount: Int,

    @ManyToOne
    var account: Account,

    @ManyToOne
    @JoinColumn(name = "parent_id")
    var parentFolder: Folder?,

    @OneToMany(mappedBy = "parentFolder")
    var children: MutableList<Folder>? = mutableListOf()
) : BaseTimeEntity() {
}
