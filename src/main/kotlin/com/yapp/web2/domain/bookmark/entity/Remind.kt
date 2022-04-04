package com.yapp.web2.domain.bookmark.entity

class Remind() {

    constructor(remindTime: String, fcmToken: String) : this() {
        this.remindTime = remindTime
        this.fcmToken = fcmToken
    }

    var remindTime: String? = null
    var fcmToken: String? = null
    var remindCheck: Boolean = false
    var remindStatus: Boolean = false
}