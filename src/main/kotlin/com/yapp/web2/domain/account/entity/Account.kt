package com.yapp.web2.domain.account.entity

import com.yapp.web2.domain.BaseTimeEntity
import com.yapp.web2.domain.folder.entity.AccountFolder
import com.yapp.web2.domain.notification.entity.Notification
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
class Account(
    var email: String
) : BaseTimeEntity() {

    companion object {
        fun requestToAccount(dto: AccountLoginRequest): Account {
            return Account(dto.email, dto.imageUrl, dto.name, dto.socialType)
        }
    }

    constructor(email: String, image: String, nickname: String, socialType: String) : this(email) {
        this.image = image
        this.nickname = nickname
        this.socialType = socialType
    }

    var password: String? = null
    var nickname: String = ""
    var sex: String = ""
    var age: Int? = null
    var socialType: String = "none"

    // TODO: 디폴트 사진 url 추가하기
    var image: String = ""

    var backgroundColor: String = "black"
    var remindToggle: Boolean = true
    var remindNotiCheck: Boolean = true

    @OneToMany(mappedBy = "account")
    var accountFolderList: MutableList<AccountFolder>? = mutableListOf()

    @OneToMany(mappedBy = "account")
    var notifications: MutableList<Notification>? = mutableListOf()

    class AccountLoginRequest(
        @field: NotEmpty(message = "이메일을 입력해주세요")
        val email: String,
        @field: NotEmpty(message = "닉네임을 입력해주세요")
        val name: String,
        val imageUrl: String,
        @field: NotEmpty(message = "소셜타입을 입력해주세요")
        val socialType: String
    )
}
