package com.yapp.web2.exception.custom

import com.yapp.web2.exception.BusinessException
import com.yapp.web2.util.ExceptionMessage

class FolderNotRootException : BusinessException(ExceptionMessage.FOLDER_IS_NOT_ROOT)