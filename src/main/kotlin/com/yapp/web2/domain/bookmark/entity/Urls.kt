package com.yapp.web2.domain.bookmark.entity

import java.time.LocalDate

data class Urls(
    var title: String = "default",
    var remindTime: LocalDate? = null,
    var saveTime: LocalDate = LocalDate.now(),
    var deleteTime: LocalDate? = null,
    var clickCount: Int = 0,
    var deleted: Boolean? = null
)