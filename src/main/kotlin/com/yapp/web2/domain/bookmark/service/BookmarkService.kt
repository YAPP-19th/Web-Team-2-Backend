package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.entity.Url
import com.yapp.web2.domain.bookmark.entity.UrlDto
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.BusinessException
import com.yapp.web2.exception.ObjectNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookmarkService(
    private val bookmarkRepository: BookmarkRepository,
    private val folderRepository: FolderRepository
) {
    @Transactional
    fun addBookmark(folderId: Long, urlDto: UrlDto): Bookmark {
        // TODO: Order 어떻게 해줄건지? --> 폴더에 가지고 있는 북마크의 수를 저장하고 가져오기로?
        // TODO: 토큰을 통해 userId 가져오기.
        checkFolderAbsence(folderId)
        val toSaveUrl = urlDtoToUrl(urlDto, 0)
        checkSameUrl(toSaveUrl, folderId)

        return bookmarkRepository.save(Bookmark(1, 1, toSaveUrl))
    }

    private fun checkFolderAbsence(folderId: Long) {
        if(folderRepository.findById(folderId).isEmpty) throw ObjectNotFoundException("해당 폴더가 존재하지 않습니다.")
    }

    private fun checkSameUrl(url: Url, folderId: Long) {
        val bookmarkList = bookmarkRepository.findAllByFolderId(folderId).toMutableList()
        for (bookmark in bookmarkList) {
            if (bookmark.urlInformation.link == url.link) throw BusinessException("똑같은 게 있어요.")
        }
    }

    private fun urlDtoToUrl(urlDto: UrlDto, order: Int): Url {
        return Url(urlDto.url, urlDto.title, order)
    }

    private fun urlToUrlDto(url: Url): UrlDto {
        return UrlDto(url.link, url.title)
    }
}