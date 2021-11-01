package com.yapp.web2.domain.bookmark.entity

import java.time.LocalDateTime

data class Information(
    val link: String,
    var title: String?,
    var remindTime: LocalDateTime?
) {
    var clickCount: Int = 0
    var deleteTime: LocalDateTime? = null
    var deleted: Boolean = false
    val saveTime: LocalDateTime = LocalDateTime.now()

    constructor(link: String, title: String, remindTime: LocalDateTime?, clickCount: Int) : this(link, title, remindTime) {
        this.clickCount = clickCount
    }
}
