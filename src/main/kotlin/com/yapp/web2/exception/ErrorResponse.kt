package com.yapp.web2.exception

data class ErrorResponse(
        val message: String? = null,
        val errors: MutableList<Error>? = mutableListOf()
) {
    private constructor(message: String) : this(message, null)

    companion object {
        fun of(message: String?) = of(message, null)
        fun of(message: String?, errors: MutableList<Error>?) = ErrorResponse(message, errors)
    }
}

data class Error(
        var field: String? = null,
        var message: String? = null,
        var value: Any? = null
)
