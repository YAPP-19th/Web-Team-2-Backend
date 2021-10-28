package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.entity.Url
import com.yapp.web2.domain.bookmark.entity.UrlDto
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.BusinessException
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
        if(folderRepository.findById(folderId).isEmpty) throw BusinessException("없어요.")
        val url = urlDtoToUrl(urlDto, 0)
        return bookmarkRepository.save(Bookmark(1, 1, url))
    }

    private fun urlDtoToUrl(urlDto: UrlDto, order: Int): Url {
        return Url(urlDto.url, urlDto.title, order)
    }

    private fun urlToUrlDto(url: Url): UrlDto {
        return UrlDto(url.url, url.title)
    }
}