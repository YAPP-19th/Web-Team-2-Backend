package com.yapp.web2.exception.custom

import com.yapp.web2.exception.BusinessException
import com.yapp.web2.util.ExceptionMessage

class NotSameRootFolderException : BusinessException(ExceptionMessage.NOT_SAME_ROOT_FOLDER)