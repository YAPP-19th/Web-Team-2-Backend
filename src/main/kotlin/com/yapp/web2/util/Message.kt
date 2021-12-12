package com.yapp.web2.util

class Message {
    companion object {
        var SUCCESS: String = "success"

        var NO_REFRESH_TOKEN = "Refresh-Token이 존재하지 않습니다"

        var REFRESH_TOKEN_MISMATCH = "Refresh-Token이 일치하지 않습니다"

        var PREFIX_MISMATCH = "접미사가 존재하지 않습니다"

        var ACCESS_TOKEN_EXPIRED = "AccessToken이 만료되었습니다"

        var WRONG_TOKEN_FORM = "형식에 어긋난 토큰입니다"

        var NULL_TOKEN = "값이 존재하지 않습니다"

        var EXIST_NAME = "이미 존재하는 닉네임입니다"

        var AVAILABLE_NAME = "사용가능한 닉네임입니다"

        var CLICK = "카운트가 증가되었습니다"

        var SAVED = "저장되었습니다"

        var DELETED = "휴지통으로 이동되었습니다"

        var UPDATED = "업데이트 되었습니다"

        var MOVED = "이동 되었습니다"
    }
}