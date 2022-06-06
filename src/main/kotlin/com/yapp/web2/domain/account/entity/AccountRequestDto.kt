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
        val fcmToken: String
    )

    @ApiModel(description = "회원가입 할 때 이메일 Request DTO")
    class SignUpEmailRequest(
        @ApiModelProperty(value = "이메일", required = true, example = "a@gmail.com")
        @field: Email(regexp = "^(.+)@(\\S+)$", message = "이메일 주소가 올바르지 않습니다")
        @field: NotBlank(message = "이메일을 입력해주세요")
        val email: String
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

    @ApiModel(description = "현재 비밀번호 조회 Request DTO")
    class CurrentPassword(
        @ApiModelProperty(value = "현재 비밀번호", required = true, example = "1234567!")
        @field: NotBlank(message = "현재 비밀번호를 입력해주세요")
        val currentPassword: String
    )

    @ApiModel(description = "비밀번호 변경 Request DTO")
    class PasswordChangeRequest(
        @ApiModelProperty(value = "기존 비밀번호", required = true, example = "1234567!")
        @field: NotBlank(message = "기존 비밀번호를 입력해주세요")
        @field: CustomPassword
        val currentPassword: String,

        @ApiModelProperty(value = "새 비밀번호", required = true, example = "12345678!")
        @field: NotBlank(message = "새 비밀번호를 입력해주세요")
        @field: CustomPassword
        val newPassword: String
    )

    @ApiModel(description = "비밀번호 재설정 - 이메일 주소 확인 Request DTO")
    class EmailCheckRequest(
        @ApiModelProperty(value = "이메일 주소", required = true, example = "a@a.com")
        @field: NotBlank(message = "이메일을 입력해주세요")
        val email: String
    )

    @ApiModel(description = "배경 색상 변경 Request DTO")
    class ChangeBackgroundColorRequest(
        @ApiModelProperty(value = "변경할 색상 이미지 url", required = true, example = "https://aaa.com")
        val changeUrl: String
    )

    @ApiModel(description = "FCM Token Request DTO")
    class FcmToken(
        @ApiModelProperty(value = "등록할 FCM Token 값", required = true, example = "fvczxj3AcxzcndmVf-sdfd..")
        val fcmToken: String
    )

}