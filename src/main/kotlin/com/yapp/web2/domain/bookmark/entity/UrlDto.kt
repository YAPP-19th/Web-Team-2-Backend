package com.yapp.web2.domain.bookmark.entity

data class UrlDto(
    var url: String
) {
    var title: String = ""

    constructor(url: String, title: String) : this(url) {
        this.title = title
    }
}