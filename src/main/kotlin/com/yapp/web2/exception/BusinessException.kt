package com.yapp.web2.exception

import java.lang.RuntimeException

open class BusinessException(message: String) : RuntimeException(message)