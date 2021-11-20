package com.yapp.web2.domain.user.entity

import com.yapp.web2.domain.BaseTimeEntity
import com.yapp.web2.domain.folder.entity.AccountFolder
import com.yapp.web2.domain.notification.entity.Notification
import javax.persistence.*

@Entity
class Account(
    var email: String
) : BaseTimeEntity() {

    constructor(email: String, image: String, nickname: String, socialType: String) : this(email) {
        this.image = image
        this.nickname = nickname
        this.socialType = socialType
    }

    var password: String? = null
    var nickname: String = "" // 구글로그인은 사용자 이름이 되면 좋겠다.
    var sex: String = ""
    var age: Int? = null
    var socialType: String = "none"

    // TODO: 디폴트 사진 url 추가하기
    var image: String = ""

    // TODO: 기본값 체크
    var backgroundColor: String = "black"
    var remindToggle: Boolean = true
    var remindNotiCheck: Boolean = true

    @OneToMany(mappedBy = "account")
    var accounts: MutableList<AccountFolder>? = mutableListOf()

    @OneToMany(mappedBy = "account")
    var notifications: MutableList<Notification>? = mutableListOf()
}
