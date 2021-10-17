package com.yapp.web2.domain.bookmark.entity

import java.time.LocalDate

data class Urls(
    var title: String,
    var remindTime: LocalDate?,
    var saveTime: LocalDate,
    var deleteTime: LocalDate?,
    var clickCount: Int = 0,
    var order: Int,
    var deleted: Boolean?,
)