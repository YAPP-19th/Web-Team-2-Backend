package com.yapp.web2.domain.bookmark.entity

import org.springframework.data.mongodb.core.mapping.Document
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Document(collection = "Bookmark")
class Bookmark(
    var userId: Long,
    var folderId: Long,
    var information: Information
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
}