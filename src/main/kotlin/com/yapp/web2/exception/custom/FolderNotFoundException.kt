package com.yapp.web2.exception.custom

import com.yapp.web2.exception.BusinessException
import com.yapp.web2.util.ExceptionMessage

class FolderNotFoundException : BusinessException(ExceptionMessage.FOLDER_NOT_FOUND)
