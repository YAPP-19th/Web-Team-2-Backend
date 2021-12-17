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
    @Column(unique = true, nullable = true, columnDefinition = "회원 이메일", length = 255)
    var email: String
) : BaseTimeEntity() {

    companion object {
        fun requestToAccount(dto: AccountLoginRequest): Account {
            return Account(dto.email, dto.image, dto.name, dto.socialType)
        }

        const val BASIC_IMAGE_URL: String = "https://yapp-bucket-test.s3.ap-northeast-2.amazonaws.com/basicImage.png"
    }

    constructor(email: String, image: String, nickname: String, socialType: String) : this(email) {
        this.image = image
        this.name = nickname
        this.socialType = socialType
    }

    @Column(nullable = true, columnDefinition = "패스워드")
    var password: String? = null

    @Column(nullable = true, columnDefinition = "이름")
    var name: String = ""

    @Column(nullable = true, columnDefinition = "성별", length = 10)
    var sex: String = ""

    @Column(nullable = true, columnDefinition = "나이", length = 5)
    var age: Int? = null

    @Column(nullable = true, columnDefinition = "소셜로그인 타입", length = 20)
    var socialType: String = "none"

    @Column(columnDefinition = "FCM Token", length = 255)
    var fcmToken: String? = null

    @Column(columnDefinition = "리마인드 주기 일자", length = 10)
    var remindCycle: String? = "7"

    @Column(nullable = true, columnDefinition = "계정 이미지 URL", length = 255)
    var image: String = BASIC_IMAGE_URL

    @Column(columnDefinition = "배경 색상", length = 30)
    var backgroundColor: String = "black"

    @Column(columnDefinition = "리마인드 토글(알림 수신)", length = 10)
    var remindToggle: Boolean = true

    @Column(nullable = true, columnDefinition = "리마인드 수신 확인", length = 10)
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
        val image: String,
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
