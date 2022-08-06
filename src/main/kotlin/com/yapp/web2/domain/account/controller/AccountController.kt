package com.yapp.web2.domain.account.controller

import com.amazonaws.services.s3.model.AmazonS3Exception
import com.yapp.web2.config.S3Client
import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.entity.AccountRequestDto
import com.yapp.web2.domain.account.service.AccountService
import com.yapp.web2.security.jwt.TokenDto
import com.yapp.web2.util.ControllerUtil
import com.yapp.web2.util.Message
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/user")
class AccountController(
    private val accountService: AccountService,
    private val s3Client: S3Client
) {
    companion object {
        private const val DIR_NAME = "static"
        private val log = LoggerFactory.getLogger(AccountController::class.java)
    }

    @ApiOperation("프로필 조회 API")
    @GetMapping("/profileInfo")
    fun getProfile(request: HttpServletRequest): ResponseEntity<Account.AccountProfile> {
        val token = ControllerUtil.extractAccessToken(request)
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getProfile(token))
    }

    @ApiOperation("리마인드 조회 API")
    @GetMapping("/remindInfo")
    fun getRemind(request: HttpServletRequest): ResponseEntity<Account.RemindElements> {
        val token = ControllerUtil.extractAccessToken(request)
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getRemindElements(token))
    }

    @ApiOperation("소셜로그인 API")
    @PostMapping("/oauth2Login")
    fun oauth2Login(
        @RequestBody @ApiParam(value = "회원 정보", required = true) request: Account.AccountProfile
    ): ResponseEntity<Account.AccountLoginSuccess> {
        val loginSuccess = accountService.oauth2LoginUser(request)
        return ResponseEntity.status(HttpStatus.OK).body(loginSuccess)
    }

    @ApiOperation(value = "토큰 재발급 API")
    @GetMapping("/reIssuanceAccessToken")
    fun reIssuanceAccessToken(request: HttpServletRequest): ResponseEntity<TokenDto> {
        val accessToken = ControllerUtil.extractAccessToken(request)
        val refreshToken = ControllerUtil.extractRefreshToken(request)
        val tokenDto = accountService.reIssuedAccessToken(accessToken, refreshToken)
        return ResponseEntity.status(HttpStatus.OK).body(tokenDto)
    }

    @ApiOperation(value = "프로필 이미지 변경 API")
    @PostMapping("/uploadProfileImage")
    fun uploadProfileImage(@RequestBody image: MultipartFile): ResponseEntity<Account.ImageUrl> {
        var imageUrl: Account.ImageUrl = Account.ImageUrl("")
        try {
            imageUrl = Account.ImageUrl(s3Client.upload(image, DIR_NAME))
        } catch (e: AmazonS3Exception) {
            log.warn("Amazon S3 error (fileName: {}): {}", image.originalFilename, e.message, e)
        }
        return ResponseEntity.status(HttpStatus.OK).body(imageUrl)
    }

    @ApiOperation(value = "프로필 편집 API")
    @PostMapping("/changeProfile")
    fun changeProfile(
        request: HttpServletRequest,
        @RequestBody @Valid @ApiParam(value = "프로필 이미지, 닉네임 정보")
        profileChanged: Account.ProfileChanged
    ): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        accountService.changeProfile(token, profileChanged)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation(value = "닉네임 비교 API")
    @PostMapping("/nickNameCheck")
    fun nickNameCheck(
        request: HttpServletRequest,
        @RequestBody @Valid @ApiParam(value = "비교할 닉네임") nickName: Account.NextNickName
    ): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        val result = accountService.checkNickNameDuplication(token, nickName)
        return ResponseEntity.status(HttpStatus.OK).body(result)
    }

    @ApiOperation(value = "닉네임 변경 API")
    @PostMapping("/nickNameChange")
    fun nickNameChange(
        request: HttpServletRequest,
        @RequestBody @ApiParam(value = "변경할 닉네임") nickName: Account.NextNickName
    ): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        accountService.changeNickName(token, nickName)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation(value = "배경 색상 변경 API")
    @PostMapping("/changeBackgroundColor")
    fun changeBackgroundColor(
        request: HttpServletRequest,
        @RequestBody @ApiParam(value = "변경할 색상 정보")
        dto: AccountRequestDto.ChangeBackgroundColorRequest
    ): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        accountService.changeBackgroundColor(token, dto)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation(value = "익스텐션 버전 조회 API")
    @GetMapping("/{extensionVersion}")
    fun checkExtensionVersion(@PathVariable extensionVersion: String): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.checkExtension(extensionVersion))
    }

    @GetMapping("/invite/{folderToken}")
    fun acceptInvitation(request: HttpServletRequest, @PathVariable folderToken: String): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        accountService.acceptInvitation(token, folderToken)
        return ResponseEntity.status(HttpStatus.OK).body("good")
    }

    @ApiOperation("회원가입 API")
    @PostMapping("/signUp")
    fun singUp(
        @RequestBody @Valid @ApiParam(value = "회원가입 정보", required = true)
        request: AccountRequestDto.SignUpRequest
    ): ResponseEntity<Account.AccountLoginSuccess> {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.signUp(request))
    }

    @ApiOperation("회원가입 시 이메일 존재여부 확인 API")
    @PostMapping("/signUp/emailCheck")
    fun checkEmail(
        @RequestBody @Valid @ApiParam(value = "회원가입 시 등록 이메일", required = true)
        request: AccountRequestDto.SignUpEmailRequest
    ): ResponseEntity<String> {
        return when (accountService.checkEmail(request)) {
            true -> ResponseEntity.status(HttpStatus.CONFLICT).body(Message.EXIST_EMAIL)
            false -> ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
        }
    }

    @ApiOperation("FCM Token 설정 API")
    @PostMapping("/fcm-token")
    fun registerFcmToken(
        request: HttpServletRequest,
        @RequestBody @Valid @ApiParam(value = "FCM Token") dto: AccountRequestDto.FcmToken
    ): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        accountService.registerFcmToken(token, dto)

        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation("일반 로그인 API")
    @PostMapping("/signIn")
    fun signIn(
        @RequestBody @Valid @ApiParam(value = "로그인 정보", required = true)
        request: AccountRequestDto.SignInRequest
    ): ResponseEntity<Account.AccountLoginSuccess> {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.signIn(request))
    }

    @ApiOperation(value = "현재 비밀번호와 입력받은 비밀번호 비교 API")
    @PostMapping("/passwordCheck")
    fun comparePassword(
        request: HttpServletRequest,
        @RequestBody @Valid @ApiParam(value = "현재(기존) 비밀번호") dto: AccountRequestDto.CurrentPassword
    ): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        return ResponseEntity.status(HttpStatus.OK).body(accountService.comparePassword(token, dto))
    }

    @ApiOperation(value = "비밀번호 변경 API")
    @PatchMapping("/password")
    fun changePassword(
        request: HttpServletRequest,
        @RequestBody @Valid @ApiParam(value = "기존 비밀번호와 새 비밀번호") dto: AccountRequestDto.PasswordChangeRequest
    ): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        return ResponseEntity.status(HttpStatus.OK).body(accountService.changePassword(token, dto))
    }

    @ApiOperation(value = "회원 탈퇴 API")
    @DeleteMapping("/unregister")
    fun deleteAccount(request: HttpServletRequest): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        accountService.softDelete(token)

        return ResponseEntity.status(HttpStatus.OK).body(Message.DELETE_ACCOUNT_SUCCEED)
    }

    @ApiOperation(value = "비밀번호 재설정 - 이메일 존재 여부 확인 API")
    @PostMapping("/password/emailCheck")
    fun checkEmailExist(
        request: HttpServletRequest,
        @RequestBody @Valid @ApiParam(value = "이메일 주소") dto: AccountRequestDto.EmailCheckRequest
    ): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        return ResponseEntity.status(HttpStatus.OK).body(accountService.checkEmailExist(token, dto))
    }

    @ApiOperation(value = "비밀번호 재설정 - 임시 비밀번호 생성 및 메일 발송 API")
    @PostMapping("/password/reset")
    fun sendTempPasswordToEmail(request: HttpServletRequest): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        val tempPassword = accountService.createTempPassword()
        accountService.updatePassword(token, tempPassword)
        accountService.sendMail(token, tempPassword)

        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

}