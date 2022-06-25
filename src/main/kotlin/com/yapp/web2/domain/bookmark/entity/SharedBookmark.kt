package com.yapp.web2.domain.bookmark.entity

import com.yapp.web2.domain.account.entity.Account

class SharedBookmark : BookmarkInterface {
    var remindAccountList = mutableListOf<Long?>()
    var rootFolderId: Long? = -1

    constructor(
        account: Account,
        link: String,
        title: String,
        image: String?,
        description: String,
        rootFolderId: Long?
    ) : super(account, link, title, image, description) {
        this.rootFolderId = rootFolderId
    }

    constructor()

    override fun moveFolder(nextFolderId: Long) {
        this.folderId = nextFolderId
    }

    override fun remindOn(remindElement: Long) {
        this.remindAccountList.add(remindElement)
    }

    override fun remindOff(remindElement: Long) {
        this.remindAccountList.remove(remindElement)
    }

    override fun remindOff() {
        throw RuntimeException()
    }

    fun isSameRootFolderId(folderId: Long?): Boolean {
        return this.rootFolderId == folderId
    }
}