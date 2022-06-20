package com.yapp.web2.util

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.AccountFolder
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import org.springframework.boot.CommandLineRunner

import java.time.LocalDateTime
import javax.transaction.Transactional

open class DummyDataInsertRunner(
    private val bookmarkRepository: BookmarkRepository,
    private val folderRepository: FolderRepository,
    private val accountRepository: AccountRepository
) : CommandLineRunner {
    companion object {
        const val dohyun: Long = 8
        const val jaegu: Long = 7
    }

    @Transactional
    override fun run(vararg args: String?) {
        val doh = accountRepository.findById(dohyun).get()
        val jae = accountRepository.findById(jaegu).get()
        defaultData(doh)
        defaultData(jae)
    }

    fun defaultData(account: Account) {
        for (i in 1..5) {
            var name = "testFolder$i"
            var parentFolder = Folder(name, 0, 0, null)
            var accountFolder = AccountFolder(account, parentFolder)
            parentFolder.folders!!.add(accountFolder)

            for (j in 1..5) {
                var childName = "testChildFolder$j"
                var childFolder = Folder(childName, j - 1, 0, parentFolder)
                childFolder = folderRepository.save(childFolder)

                for (k in 1..10) {
                    var testBookmark = Bookmark(account.id!!, childFolder.id, "testLink$k")
                    var testTrashBookmark = Bookmark(account.id!!, childFolder.id, "testTrashLink$k")

                    testTrashBookmark.deleted = true
                    testTrashBookmark.deleteTime = LocalDateTime.now()

                    bookmarkRepository.save(testBookmark)
                    bookmarkRepository.save(testTrashBookmark)
                }
                parentFolder.children!!.add(childFolder)
            }
            folderRepository.save(parentFolder)
        }
    }
}