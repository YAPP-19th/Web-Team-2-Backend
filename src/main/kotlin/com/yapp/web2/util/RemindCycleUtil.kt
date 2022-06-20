package com.yapp.web2.util

import com.yapp.web2.exception.custom.RemindCycleValidException

enum class RemindCycleUtil(val days: Int) {

    THREE_DAYS(3),
    SEVEN_DAYS(7),
    FOURTEEN_DAYS(14),
    THIRTY_DAYS(30);

    companion object {
        fun validRemindCycle(remindCycle: Int): Boolean {
            for (cycle in RemindCycleUtil.values()) {
                if (cycle.days == remindCycle) {
                    return true
                }
            }
            throw RemindCycleValidException()
        }
    }
}