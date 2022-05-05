package com.yapp.web2.domain.account.entity

import com.yapp.web2.common.CustomPassword
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

class AccountRequestDto {

    @ApiModel(description = "회원가입 Request DTO")
    class SignUpRequest(

        @ApiModelProperty(value = "이메일", required = true, example = "a@gmail.com")
        @field: Email(regexp = "^(.+)@(\\S+)$", message = "이메일의 형식을 올바르게 입력해주세요")
        @field: NotBlank(message = "이메일을 입력해주세요")
        val email: String,

        @ApiModelProperty(value = "비밀번호", required = true, example = "!1234567")
        @field: NotBlank(message = "비밀번호를 입력해주세요")
        @field: CustomPassword
        val password: String,

        @ApiModelProperty(value = "FCM 토큰", required = true, example = "dOOUnnp-iBs:APA91bF1i7mobIF7kEhi3aVlFuv6A5--P1S...")
        @field: NotBlank(message = "FCM 토큰이 존재하지 않습니다.")
        val fcmToken: String
    )

    @ApiModel(description = "로그인 Request DTO")
    class SignInRequest(
        @ApiModelProperty(value = "이메일", required = true, example = "a@gmail.com")
        @field: Email(regexp = "^(.+)@(\\S+)$", message = "이메일의 형식을 올바르게 입력해주세요")
        @field: NotBlank(message = "이메일을 입력해주세요")
        val email: String,

        @ApiModelProperty(value = "비밀번호", required = true, example = "!1234567")
        @field: CustomPassword
        val password: String
    )


    class CurrentPassword(
        @field: NotBlank(message = "현재 비밀번호를 입력해주세요")
        val currentPassword: String
    )

    class PasswordChangeRequest(
        @field: NotBlank(message = "기존 비밀번호를 입력해주세요")
        @field: CustomPassword
        val currentPassword: String,

        @field: NotBlank(message = "새 비밀번호를 입력해주세요")
        @field: CustomPassword
        val newPassword: String
    )

}