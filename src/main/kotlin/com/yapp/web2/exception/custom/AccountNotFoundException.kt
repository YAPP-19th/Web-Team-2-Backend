package com.yapp.web2.exception.custom

import com.yapp.web2.exception.BusinessException
import com.yapp.web2.util.ExceptionMessage

class AccountNotFoundException : BusinessException(ExceptionMessage.ACCOUNT_NOT_EXIST)