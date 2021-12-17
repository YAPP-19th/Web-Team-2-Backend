package com.yapp.web2.domain.account.entity

import com.yapp.web2.domain.BaseTimeEntity
import com.yapp.web2.domain.folder.entity.AccountFolder
import com.yapp.web2.domain.notification.entity.Notification
import com.yapp.web2.security.jwt.TokenDto
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

@Entity
class Account(
    var email: String
) : BaseTimeEntity() {

    companion object {
        fun requestToAccount(dto: AccountLoginRequest): Account {
            return Account(dto.email, dto.imageUrl, dto.name, dto.socialType)
        }

        const val BASIC_IMAGE_URL: String = "https://yapp-bucket-test.s3.ap-northeast-2.amazonaws.com/basicImage.png"
    }

    constructor(email: String, image: String, nickname: String, socialType: String) : this(email) {
        this.image = image
        this.name = nickname
        this.socialType = socialType
    }

    var password: String? = null
    var name: String = ""
    var sex: String = ""
    var age: Int? = null
    var socialType: String = "none"
    var fcmToken: String? = null
    var remindCycle: Int? = 7

    // TODO: 디폴트 사진 url 추가하기
    var image: String = BASIC_IMAGE_URL

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

    class AccountLoginSuccess(
        tokenDto: TokenDto, account: Account
    ) {
        val accessToken = tokenDto.accessToken
        val refreshToken = tokenDto.refreshToken
        val email = account.email
        val name = account.name
        val image = account.image
        val socialType = account.socialType
        val remindCycle = account.remindCycle
        val remindToggle = account.remindToggle
    }

    class NextNickName(
        @field: Size(max = 20)
        val nickName: String
    )
}
