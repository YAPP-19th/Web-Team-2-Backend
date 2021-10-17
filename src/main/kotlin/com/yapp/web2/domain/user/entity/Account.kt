package com.yapp.web2.domain.user.entity

import com.yapp.web2.domain.BaseTimeEntity
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.notification.entity.NotifIcation
import javax.persistence.*

@Entity
class Account(
    var email: String,
    var password: String?,
    var nickname: String,
    var sex: String?,
    var age: Int?,

    // TODO: 디폴트 사진 url 추가하기
    var image: String,

    // TODO: 기본값 체크
    var backgroundColor: String,
    var remindToggle: Boolean,
    var remindNotiCheck: Boolean,

    @OneToMany(mappedBy = "account")
    var children: MutableList<Folder>?,

    @OneToMany(mappedBy = "account")
    var notifications: MutableList<NotifIcation>?

) : BaseTimeEntity() {

}
