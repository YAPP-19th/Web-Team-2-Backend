package com.yapp.web2.domain.bookmark.entity

import java.time.LocalDate

data class Url(
    var url: String,
    var title: String,
    var order: Int
) {
    var remindTime: LocalDate? = null
    var deleteTime: LocalDate? = null
    var clickCount: Int = 0
    var deleted: Boolean = false
    val saveTime: LocalDate = LocalDate.now()

    constructor(url: String, title: String, order: Int, deleted: Boolean) : this(url, title, order) {
        this.deleted = deleted
    }
}