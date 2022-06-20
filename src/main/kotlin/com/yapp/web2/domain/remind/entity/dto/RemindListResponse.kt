package com.yapp.web2.domain.remind.entity.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDateTime

@ApiModel(description = "리마인드 리스트 조회 Response")
class RemindListResponse(

    @ApiModelProperty(value = "북마크 ID", required = true, example = "61bdbbaa72b0f85372ad57c8")
    val id: String,

    @ApiModelProperty(value = "북마크 제목", required = true, example = "북마크 정보~")
    val title: String,

    @ApiModelProperty(value = "리마인드 발송 시각(13시 고정)", required = true, example = "2021-12-25T13:00")
    val pushTime: LocalDateTime
)