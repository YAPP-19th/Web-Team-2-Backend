package com.yapp.web2.exception.custom

import com.yapp.web2.exception.BusinessException
import com.yapp.web2.util.Message

class ExistNameException: BusinessException(Message.EXIST_NAME)