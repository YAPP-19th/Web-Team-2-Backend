package com.yapp.web2.domain.folder.entity

enum class SharedType {
    EDIT {
        override fun inversionState(): SharedType {
            return CLOSED_EDIT
        }
    },
    BLOCK_EDIT {
        override fun inversionState(): SharedType {
            return CLOSED_BLOCK_EDIT
        }
    },
    CLOSED_EDIT {
        override fun inversionState(): SharedType {
            return EDIT
        }
    },
    CLOSED_BLOCK_EDIT {
        override fun inversionState(): SharedType {
            return BLOCK_EDIT
        }
    } ;

    abstract fun inversionState(): SharedType
}