package com.yapp.web2.domain.bookmark.repository

import com.yapp.web2.domain.bookmark.entity.Bookmark
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

class BookmarkRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : MongoTemplateRepository {

    /**
     * Bookmark의 remindList 내에서 userId 기준 아래 조건에 일치하는 북마크 리스트 조회
     *  - 리마인드 발송된 상태(remindStatus = true)
     *  - 미확인 상태(remindCheck = false)
     */
    override fun findAllBookmarkByUserIdAndRemindsInRemindList(userId: Long): List<Bookmark> {
        val query = Query()

        query.addCriteria(
            Criteria.where("remindList").elemMatch(
                Criteria.where("userId").`is`(userId)
                    .and("remindCheck").`is`(false)
                    .and("remindStatus").`is`(true)
            )
        )
        setFieldsInclude(query)

        return mongoTemplate.find(query, Bookmark::class.java)
    }

    /* 매핑할 컬럼 추가 */
    private fun setFieldsInclude(query: Query) {
        query.fields().include("userId")
        query.fields().include("folderId")
        query.fields().include("link")
        query.fields().include("remindList.$")
        query.fields().include("title")
    }

}