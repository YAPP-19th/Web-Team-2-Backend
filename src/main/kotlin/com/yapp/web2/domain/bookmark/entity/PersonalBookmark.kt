package com.yapp.web2.domain.bookmark.entity

import com.yapp.web2.domain.account.entity.Account
import java.time.LocalDate

class PersonalBookmark : BookmarkInterface {

    var remindTime: String? = null
    var remindCheck: Boolean = false
    var remindStatus: Boolean = false

    //using constructor NO_CONSTRUCTOR with arguments
    constructor() // 조회할 때, 빈 constructor가 존재하지 않으면 예외를 던진다.

    constructor(
        account: Account,
        link: String,
        title: String,
        image: String?,
        description: String,
        remind: Boolean
    ) : super(account, link, title, image, description) {
        if (remind) remindOn(account.remindCycle.toLong())
    }

    constructor(title: String) {
        this.title = title
    }

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