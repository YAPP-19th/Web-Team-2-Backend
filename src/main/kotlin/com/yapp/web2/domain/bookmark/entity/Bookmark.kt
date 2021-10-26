package com.yapp.web2.domain.bookmark.entity

import org.springframework.data.mongodb.core.mapping.Document
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Document(collection = "BookMark")
class Bookmark(
    var userId: Long,
    var folderId: Long,
    var url: Url
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var _id: Long = 0
}