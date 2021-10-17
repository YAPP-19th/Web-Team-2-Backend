package com.yapp.web2.domain.bookmark.entity

import org.springframework.data.mongodb.core.mapping.Document
import javax.persistence.Id

@Document(collection = "BookMark")
class Bookmark(
    @Id
    var _id: Long,
    var userId: Long,
    var folderId: Long,
    var urls: Urls
) {

}