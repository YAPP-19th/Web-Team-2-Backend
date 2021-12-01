package com.yapp.web2.exception.custom

import com.yapp.web2.exception.BusinessException
import com.yapp.web2.util.Message

class NoRefreshTokenException : BusinessException(Message.NO_REFRESH_TOKEN)

class TokenMisMatchException : BusinessException(Message.REFRESH_TOKEN_MISMATCH)

class PrefixMisMatchException : BusinessException(Message.PREFIX_MISMATCH)