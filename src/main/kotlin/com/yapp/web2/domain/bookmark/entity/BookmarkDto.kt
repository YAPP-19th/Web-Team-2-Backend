package com.yapp.web2.domain.bookmark.entity

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

class BookmarkDto {

    @ApiModel(description = "북마크 수정 DTO")
    class UpdateBookmarkDto(
        @ApiModelProperty(value = "북마크 이름", required = true, example = "Change Bookmark")
        @field:NotEmpty(message = "제목을 입력해주세요")
        var title: String,

        @ApiModelProperty(value = "북마크 설명", example = "description")
        @field:NotNull(message = "description이 null입니다!")
        var description: String
    )

    @ApiModel(description = "북마크 생성 DTO")
    data class AddBookmarkDto(

        // TODO: 2021/12/04 RequestParam 데이터 검증
        @ApiModelProperty(value = "북마크 url", required = true, example = "https://www.naver.com")
        var url: String,

        @ApiModelProperty(value = "북마크 제목", example = "Bookmark Title")
        var title: String?,

        @ApiModelProperty(value = "북마크 리마인드 여부", required = true, example = "true")
        var remind: Boolean,

        @ApiModelProperty(value = "북마크 이미지", example = "https://yapp-bucket-test.s3.ap-northeast-2.amazonaws.com/basicImage.png")
        var image: String?,

        @ApiModelProperty(value = "description", example = "설명 예제...")
        var description: String?
    )

    @ApiModel(description = "북마크 이동 API(폴더별 이동)")
    data class MoveBookmarkDto(
        val bookmarkIdList: MutableList<String>,
        @ApiModelProperty(value = "이동 후 폴더 ID", required = true, example = "2")
        val nextFolderId: Long
    )

    @ApiModel(description = "휴지통 복원 API")
    data class RestoreBookmarkRequest(

        @ApiModelProperty(value = "복원할 북마크 ID 리스트")
        val bookmarkIdList: MutableList<String>?
    )

    @ApiModel(description = "휴지통 영구삭제 API")
    data class TruncateBookmarkRequest(

        @ApiModelProperty(value = "영구삭제할 북마크 ID 리스트")
        val bookmarkIdList: MutableList<String>?
    )

    data class RemindList(
        val remindBookmarkList: List<Bookmark>
    )

    data class BookmarkIdList(
        val dotoriIdList: MutableList<String>
    )
}