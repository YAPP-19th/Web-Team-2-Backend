package com.yapp.web2.util

class Message {
    companion object {
        var SUCCESS: String = "success"

        var NO_REFRESH_TOKEN = "Refresh-Token이 존재하지 않습니다"

        var REFRESH_TOKEN_MISMATCH = "Refresh-Token이 일치하지 않습니다"

        var PREFIX_MISMATCH = "접미사가 존재하지 않습니다"

        var TOKEN_EXPIRED = "Token이 만료되었습니다"

        var WRONG_TOKEN_FORM = "형식에 어긋난 토큰입니다"

        var NULL_TOKEN = "값이 존재하지 않습니다"

        var EXIST_NAME = "이미 존재하는 닉네임입니다"

        var EXIST_USER = "이미 존재하는 회원입니다."

        var AVAILABLE_NAME = "사용가능한 닉네임입니다"

        var CLICK = "카운트가 증가되었습니다"

        var SAVED = "저장되었습니다"

        var DELETED = "휴지통으로 이동되었습니다"

        var UPDATED = "업데이트 되었습니다"

        var MOVED = "이동 되었습니다"

        var IMAGE_NOT_FOUND = "이미지가 존재하지 않습니다"

        var SAME_BOOKMARK_EXIST = "똑같은 북마크가 존재합니다"

        var OBJECT_NOT_FOUND = "개체가 존재하지 않습니다"

        var NOTIFICATION_MESSAGE = "깜빡한 도토리가 있지 않나요?"

        var OLD_EXTENSION_VERSION = "익스텐션 버전이 오래되었습니다!"

        var LATEST_EXTENSION_VERSION = "최신 버전입니다."

        var USER_PASSWORD_MISMATCH = "비밀번호가 일치하지 않습니다."

        var SAME_PASSWORD = "현재 비밀번호와 동일합니다."

        var CHANGE_PASSWORD_SUCCEED = "비밀번호가 정상적으로 변경되었습니다."

        var DELETE_ACCOUNT_SUCCEED = "정상적으로 탈퇴되었습니다."

        var PASSWORD_VALID_MESSAGE = "특수문자를 포함하여 영문 대소문자, 숫자 중 2종류 이상을 조합하여 8~16자의 비밀번호를 생성해주세요."

        var NOT_EXIST_EMAIL = "존재하지 않는 이메일 입니다."
    }
}