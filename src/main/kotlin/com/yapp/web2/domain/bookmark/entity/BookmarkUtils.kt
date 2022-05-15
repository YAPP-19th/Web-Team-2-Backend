package com.yapp.web2.domain.bookmark.entity

import com.yapp.web2.domain.account.entity.Account

class BookmarkUtils {
    companion object {
        fun sharedBookmarkToPersonalBookmark(sharedBookmark: SharedBookmark, account: Account): PersonalBookmark {
            val bookmark = PersonalBookmark()
            bookmark.parentBookmarkId = sharedBookmark.id
            bookmark.userId = account.id
            bookmark.link = sharedBookmark.link
            bookmark.title = sharedBookmark.title
            bookmark.description = sharedBookmark.description
            bookmark.image = sharedBookmark.image
            return bookmark
        }
    }
}