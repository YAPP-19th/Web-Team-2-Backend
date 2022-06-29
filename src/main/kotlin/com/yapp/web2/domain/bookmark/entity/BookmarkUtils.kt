package com.yapp.web2.domain.bookmark.entity

import com.yapp.web2.domain.account.entity.Account
import java.time.LocalDate

class BookmarkUtils {
    companion object {
        fun sharedBookmarkToPersonalBookmark(sharedBookmark: SharedBookmark, account: Account): PersonalBookmark {
            return PersonalBookmark(
                account,
                sharedBookmark.link,
                sharedBookmark.title,
                sharedBookmark.image,
                sharedBookmark.description,
                true
            )
        }
    }
}