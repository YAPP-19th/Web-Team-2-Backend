package com.yapp.web2.domain.account.entity

import com.yapp.web2.domain.BaseTimeEntity
import com.yapp.web2.domain.folder.entity.AccountFolder
import com.yapp.web2.domain.remind.entity.Remind
import com.yapp.web2.security.jwt.TokenDto
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

@Entity
class Account(
    var email: String
) : BaseTimeEntity() {

    companion object {
        fun requestToAccount(dto: AccountLoginRequest): Account {
            return Account(dto.email, dto.image, dto.name, dto.socialType, dto.fcmToken)
        }

        const val BASIC_IMAGE_URL: String = "https://yapp-bucket-test.s3.ap-northeast-2.amazonaws.com/basicImage.png"
    }

    constructor(email: String, image: String, nickname: String, socialType: String, fcmToken: String) : this(email) {
        this.image = image
        this.name = nickname
        this.socialType = socialType
        this.fcmToken = fcmToken
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
    var notifications: MutableList<Remind>? = mutableListOf()

    @ApiModel(description = "소셜로그인 DTO")
    class AccountLoginRequest(
        @ApiModelProperty(value = "이메일", required = true, example = "test@gmail.com")
        @field: NotEmpty(message = "이메일을 입력해주세요")
        val email: String,

        @ApiModelProperty(value = "닉네임", required = true, example = "도토리함")
        @field: NotEmpty(message = "닉네임을 입력해주세요")
        val name: String,

        @ApiModelProperty(value = "이미지", example = "https://yapp-bucket-test.s3.ap-northeast-2.amazonaws.com/static/61908b14-a736-46ef-9d89-3ef12ef57e38")
        val image: String,

        @ApiModelProperty(value = "소셜로그인 종류", required = true, example = "google")
        @field: NotEmpty(message = "소셜타입을 입력해주세요")
        val socialType: String,

        @ApiModelProperty(value = "FCM 토큰", required = true, example = "dOOUnnp-iBs:APA91bF1i7mobIF7kEhi3aVlFuv6A5--P1S...")
        //@field: NotEmpty(message = "FCM 토큰을 입력해주세요")
        val fcmToken: String
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
        val fcmToken = account.fcmToken
    }

    class NextNickName(
        @field: Size(max = 20)
        val nickName: String
    )
}
