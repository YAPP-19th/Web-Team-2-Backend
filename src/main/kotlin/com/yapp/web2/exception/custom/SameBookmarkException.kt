package com.yapp.web2.exception.custom

import com.yapp.web2.exception.BusinessException
import com.yapp.web2.util.Message

class SameBookmarkException : BusinessException(Message.SAME_BOOKMARK_EXIST)