package com.yapp.web2.domain.bookmark.repository

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.entity.Remind
import org.junit.After
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate

@RunWith(SpringRunner::class)
@ActiveProfiles("local")
@DataMongoTest
class MongoDBTest {
    @Autowired
    lateinit var bookmarkRepository: BookmarkRepository

    @After
    fun cleanUp() {
        bookmarkRepository.deleteAll()
    }

    @Test
    fun `북마크를 저장한다`() {
        bookmarkRepository.save(Bookmark(2, 1, "test"))
    }

    @Test
    fun `북마크에서 RemindList중 하나를 조회해온다`() {
        val account = Account("testEmail", "test")
        account.id = 1
        account.fcmToken = "testToken"
        val save = bookmarkRepository.save(Bookmark(2, 1, "test"))
        val save2 = bookmarkRepository.save(Bookmark(2, 2, "test2"))

        save2.remindList.add(Remind(account))

        account.fcmToken = "testToken2"
        save.remindList.add(Remind(account))
        save2.remindList.add(Remind(account))

        bookmarkRepository.save(save)
        bookmarkRepository.save(save2)

        val bookmarkList = bookmarkRepository.findAllBookmarkByFcmToken("testToken")
        val bookmarkList2 = bookmarkRepository.findAllBookmarkByFcmToken("testToken2")

        assertEquals(bookmarkList2.size, 2)
        assertEquals(bookmarkList.size, 1)
    }
}