package com.yapp.web2.exception.custom

import com.yapp.web2.exception.BusinessException
import com.yapp.web2.util.ExceptionMessage

class PasswordMismatchException : BusinessException(ExceptionMessage.PASSWORD_DIFFERENT_EXCEPTION)