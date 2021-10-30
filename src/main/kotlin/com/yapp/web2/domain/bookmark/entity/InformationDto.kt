package com.yapp.web2.domain.bookmark.entity

data class InformationDto(
    var url: String
) {
    var title: String? = ""
    var remind: Boolean? = false
    var clickCount: Int? = 0
    var deleted: Boolean? = false

    constructor(url: String, title: String) : this(url) {
        this.title = title
    }

    constructor(url: String, title: String, remind: Boolean) : this(url, title) {
        this.remind = remind
    }

    constructor(url: String, title: String, remind: Boolean, clickCount: Int) : this(url, title, remind) {
        this.clickCount = clickCount
    }
}