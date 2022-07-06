package com.yapp.web2.exception.custom

import com.yapp.web2.exception.BusinessException
import com.yapp.web2.util.ExceptionMessage
import com.yapp.web2.util.Message

class AlreadyExistRemindException : BusinessException(ExceptionMessage.ALREADY_EXIST_REMIND) {
}