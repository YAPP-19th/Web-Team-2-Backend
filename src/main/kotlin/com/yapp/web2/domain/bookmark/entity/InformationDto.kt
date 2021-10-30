package com.yapp.web2.domain.bookmark.entity

data class InformationDto(
    var url: String
) {
    var title: String = ""
    var id: Long? = null

    constructor(url: String, title: String) : this(url) {
        this.title = title
    }
}