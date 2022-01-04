package com.yapp.web2.exception.custom

import com.yapp.web2.exception.BusinessException
import com.yapp.web2.util.Message

class ExistNameException : BusinessException(Message.EXIST_NAME)

class ImageNotFoundException : BusinessException(Message.IMAGE_NOT_FOUND)