package com.yapp.web2.domain.bookmark.repository

import com.yapp.web2.domain.bookmark.entity.Bookmark

interface MongoTemplateRepository {

    fun findAllBookmarkByUserIdAndRemindsInRemindList(userId: Long): List<Bookmark>

}