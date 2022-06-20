package com.yapp.web2.exception

import com.yapp.web2.util.Message

open class ObjectNotFoundException() : BusinessException(Message.OBJECT_NOT_FOUND)