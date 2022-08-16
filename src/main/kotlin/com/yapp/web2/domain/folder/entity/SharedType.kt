package com.yapp.web2.domain.folder.entity

enum class SharedType {
    INVITE {
        override fun inversionState(): SharedType {
            return CLOSED_INVITE
        }
    },
    OPEN {
        override fun inversionState(): SharedType {
            return CLOSED_OPEN
        }
    },
    INVITE_AND_OPEN {
        override fun inversionState(): SharedType {
            return ALL_CLOSED
        }
    },
    CLOSED_INVITE {
        override fun inversionState(): SharedType {
            return INVITE
        }
    },
    CLOSED_OPEN {
        override fun inversionState(): SharedType {
            return OPEN
        }
    },
    ALL_CLOSED {
        override fun inversionState(): SharedType {
            return INVITE_AND_OPEN
        }
    };

    abstract fun inversionState(): SharedType
}