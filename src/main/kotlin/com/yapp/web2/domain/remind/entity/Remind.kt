package com.yapp.web2.domain.remind.entity

import com.yapp.web2.domain.BaseTimeEntity
import com.yapp.web2.domain.account.entity.Account
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class Remind(
    var remindSetTime: LocalDateTime,
    var remindCycle: LocalDateTime,

    @ManyToOne
    var account: Account
) : BaseTimeEntity() {

}