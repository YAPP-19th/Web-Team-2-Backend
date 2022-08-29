package com.yapp.web2.domain.account.entity

import com.yapp.web2.domain.BaseTimeEntity
import com.yapp.web2.domain.folder.entity.AccountFolder
import com.yapp.web2.security.jwt.TokenDto
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.springframework.transaction.annotation.Transactional
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

@Entity
class Account(
    @Column(unique = true, nullable = true, length = 255)
    var email: String
) : BaseTimeEntity() {

    @Column(nullable = true)
    var password: String? = null

    @Column(nullable = true)
    var name: String = ""

    @Column(nullable = true, length = 20)
    var socialType: String = "none"

    @Column(length = 255)
    var fcmToken: String? = null

    @Column(length = 10)
    var remindCycle: Int = 7

    @Column(nullable = true, length = 255)
    var image: String = BASIC_IMAGE_URL

    @Column(length = 20)
    var backgroundColor: String = "black"

    @Column
    var remindToggle: Boolean = true

    @Column
    var deleted: Boolean = false

    @OneToMany(mappedBy = "account")
    var accountFolderList: MutableList<AccountFolder> = mutableListOf()

    constructor(email: String, password: String) : this(email) {
        this.password = password
    }

    constructor(email: String, encryptPassword: String, fcmToken: String, name: String) : this(email) {
        this.password = encryptPassword
        this.fcmToken = fcmToken
        this.name = name
    }

    constructor(email: String, image: String, nickname: String, socialType: String, fcmToken: String) : this(email) {
        this.image = image
        this.name = nickname
        this.socialType = socialType
        this.fcmToken = fcmToken
    }

    fun addAccountFolder(accountFolder: AccountFolder) {
        this.accountFolderList.add(accountFolder)
    }

    @Transactional
    fun isInsideAccountFolder(accountFolder: AccountFolder): Boolean {
        accountFolderList.forEach {
            if (it.folder.id == accountFolder.folder.id) return true
        }
        return false
    }

    fun inverseRemindToggle(reverse: Boolean) {
        this.remindToggle = reverse
    }

    companion object {
        fun signUpToAccount(dto: AccountRequestDto.SignUpRequest, encryptPassword: String, name: String): Account {
            return Account(dto.email, encryptPassword, dto.fcmToken, name)
        }

        fun profileToAccount(dto: AccountProfile): Account {
            return Account(dto.email, dto.image, dto.name, dto.socialType, dto.fcmToken)
        }

        fun accountToProfile(account: Account): AccountProfile {
            return AccountProfile(
                account.email,
                account.name,
                account.image,
                account.socialType,
                account.fcmToken ?: ""
            )
        }

        fun accountToRemindElements(account: Account): RemindElements {
            return RemindElements(account.remindCycle, account.remindToggle)
        }

        const val BASIC_IMAGE_URL: String = "https://yapp-bucket-test.s3.ap-northeast-2.amazonaws.com/basicImage.png"
    }

    @ApiModel(description = "소셜로그인 DTO")
    class AccountProfile(
        @ApiModelProperty(value = "이메일", required = true, example = "test@gmail.com")
        @field: NotEmpty(message = "이메일을 입력해주세요")
        val email: String,

        @ApiModelProperty(value = "닉네임", required = true, example = "도토리함")
        @field: NotEmpty(message = "닉네임을 입력해주세요")
        val name: String,

        @ApiModelProperty(
            value = "이미지",
            example = "https://yapp-bucket-test.s3.ap-northeast-2.amazonaws.com/static/61908b14-a736-46ef-9d89-3ef12ef57e38"
        )
        val image: String,

        @ApiModelProperty(value = "소셜로그인 종류", required = true, example = "google")
        @field: NotEmpty(message = "소셜타입을 입력해주세요")
        val socialType: String,

        @ApiModelProperty(
            value = "FCM 토큰",
            required = true,
            example = "dOOUnnp-iBs:APA91bF1i7mobIF7kEhi3aVlFuv6A5--P1S..."
        )
        @field: NotEmpty(message = "FCM 토큰을 입력해주세요")
        val fcmToken: String
    )

    class AccountLoginSuccess(
        tokenDto: TokenDto,
        account: Account,
        isRegistered: Boolean
    ) {
        val accessToken = tokenDto.accessToken
        val refreshToken = tokenDto.refreshToken
        val email = account.email
        val name = account.name
        val image = account.image
        val socialType = account.socialType
        val remindCycle = account.remindCycle
        val remindToggle = account.remindToggle
        var isRegistered = isRegistered
    }

    class RemindElements(
        val remindCycle: Int?,
        val remindToggle: Boolean
    )

    class ProfileChanged(
        val profileImageUrl: String,
        @field: NotBlank(message = "이름을 입력해주세요")
        val name: String
    )

    class ImageUrl(
        val imageUrl: String
    )

    class NextNickName(
        @field: NotBlank(message = "이름을 입력해주세요")
        val nickName: String
    )

    fun hasAccountFolder(accountFolder: AccountFolder): Boolean {
        for (af in this.accountFolderList)
            if (accountFolder == af) return true

        return false
    }

    fun softDeleteAccount() {
        this.deleted = true
    }

    fun updateFcmToken(fcmToken: String) {
        this.fcmToken = fcmToken
    }
}