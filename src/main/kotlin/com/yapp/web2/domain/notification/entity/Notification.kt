package com.yapp.web2.domain.notification.entity

import com.yapp.web2.domain.BaseTimeEntity
import com.yapp.web2.domain.user.entity.Account
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class Notification(
    var remindSetTime: LocalDateTime,
    var remindCycle: LocalDateTime,

    @ManyToOne
    var account: Account
) : BaseTimeEntity() {

}