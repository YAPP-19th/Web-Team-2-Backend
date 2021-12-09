package com.yapp.web2.util

class Message {
    companion object {
        var SUCCESS: String = "success"

        var FOLDER_NOT_FOUND = "존재하지 않는 폴더입니다"

        var NO_REFRESH_TOKEN = "Refresh-Token이 존재하지 않습니다"

        var REFRESH_TOKEN_MISMATCH = "Refresh-Token이 일치하지 않습니다"

        var PREFIX_MISMATCH = "접미사가 존재하지 않습니다"

        var ACCESS_TOKEN_EXPIRED = "AccessToken이 만료되었습니다"

        var WRONG_TOKEN_FORM = "형식에 어긋난 토큰입니다"

        var NULL_TOKEN = "값이 존재하지 않습니다"
    }
}