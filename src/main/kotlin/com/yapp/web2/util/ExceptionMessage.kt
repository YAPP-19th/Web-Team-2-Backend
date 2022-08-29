package com.yapp.web2.util

class ExceptionMessage {

    companion object {
        const val FOLDER_NOT_FOUND = "존재하지 않는 폴더입니다."

        const val BOOKMARK_NOT_FOUND = "존재하지 않는 북마크입니다."

        const val ACCOUNT_NOT_EXIST = "존재하지 않는 계정입니다."

        const val REMIND_CYCLE_VALID_EXCEPTION = "정확한 주기 일자를 입력해주세요."

        const val FOLDER_SIZE_EXCEED_EXCEPTION = "하위 폴더는 최대 8개까지 생성을 할 수 있습니다."

        const val FOLDER_IS_NOT_ROOT = "보관함이 아닙니다."

        const val AlREADY_INVITED = "이미 초대되었습니다."

        const val NO_PERMISSION = "권한이 없습니다."

        const val NOT_SAME_ROOT_FOLDER = "동일한 보관함이 아닙니다."

        const val PASSWORD_DIFFERENT_EXCEPTION = "비밀번호가 일치하지 않습니다."

        const val NOT_FOUND_EMAIL = "가입하신 이메일 주소를 찾을 수 없습니다."

        const val ALREADY_EXIST_REMIND = "이미 리마인드가 존재합니다."
    }
}