package com.yapp.web2.exception.custom

import com.yapp.web2.exception.BusinessException
import com.yapp.web2.util.Message

class FolderNotFoundException : BusinessException(Message.FOLDER_NOT_FOUND)
