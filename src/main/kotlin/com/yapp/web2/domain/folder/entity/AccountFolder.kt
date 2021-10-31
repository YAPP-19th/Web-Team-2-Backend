package com.yapp.web2.domain.folder.entity

import com.yapp.web2.domain.user.entity.Account
import javax.persistence.*

@Entity
class AccountFolder(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @ManyToOne
    @JoinColumn(name = "account_id")
    var account: Account,

    @ManyToOne
    @JoinColumn(name = "folder_id")
    var folder: Folder
)