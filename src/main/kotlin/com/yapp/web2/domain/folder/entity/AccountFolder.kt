package com.yapp.web2.domain.folder.entity

import com.yapp.web2.domain.account.entity.Account
import javax.persistence.*

@Entity
class AccountFolder(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    var account: Account,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    var folder: Folder
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Enumerated(value = EnumType.STRING)
    var authority: Authority = Authority.NONE

    fun changeAuthority(authority: Authority) {
        this.authority = authority
    }
}