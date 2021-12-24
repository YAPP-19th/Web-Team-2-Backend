package com.yapp.web2.domain.remind.entity.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "리마인드 읽음처리 할 북마크 ID 리스트")
class ReadRemindListRequest(

    //	"bookmarkIdList" : ["61bdbbaa72b0f85372ad57c8", "61bdbbaa72b0f8551ac8adn", "72cdaa72b0f1fv34272ad57c8"]
    @ApiModelProperty(
        value = "북마크 ID 리스트",
        required= true,
        example = "bookmarkIdList : \"61bdbbaa72b0f85372ad57c8\", \"61bdbbaa72b0f8551ac8adn\" ... ")
    val bookmarkIdList: MutableList<String>
)