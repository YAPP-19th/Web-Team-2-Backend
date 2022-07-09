package com.yapp.web2.domain.bookmark.entity

import com.yapp.web2.domain.account.entity.Account
import java.time.LocalDate

class Remind() {

    constructor(userId: Long): this() {
        this.userId = userId
    }

    constructor(account: Account): this() {
        this.userId = account.id!!
        this.remindTime = LocalDate.now().plusDays(account.remindCycle.toLong()).toString()
        this.fcmToken = account.fcmToken!!
    }
    var userId: Long = -1L
    var remindTime: String = ""
    var fcmToken: String  = ""
    var remindCheck: Boolean = false
    var remindStatus: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Remind

        if (userId != other.userId) return false

        return true
    }

    override fun hashCode(): Int {
        return userId.hashCode()
    }
}