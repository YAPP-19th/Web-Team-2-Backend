package com.yapp.web2.domain.account.controller

import com.amazonaws.services.s3.model.AmazonS3Exception
import com.yapp.web2.common.CustomPassword
import com.yapp.web2.config.S3Uploader
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
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import javax.validation.Validation
import javax.validation.constraints.NotEmpty

@RestController
@RequestMapping("/api/v1/user")
class AccountController(
    private val accountService: AccountService,
    private val s3Uploader: S3Uploader
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
            imageUrl = Account.ImageUrl(s3Uploader.upload(image, DIR_NAME))
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

    @ApiOperation(value = "닉네임 조회 API")
    @PostMapping("/nickNameCheck")
    fun nickNameCheck(
        request: HttpServletRequest,
        @RequestBody @Valid @ApiParam(value = "변경할 닉네임") nickName: Account.NextNickName
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
    fun changeBackgroundColor(request: HttpServletRequest, changeUrl: String): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        accountService.changeBackgroundColor(token, changeUrl)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation(value = "익스텐션 버전 조회 API")
    @GetMapping("/{extensionVersion}")
    fun checkExtensionVersion(@PathVariable extensionVersion: String): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.checkExtension(extensionVersion))
    }

    @ApiOperation("회원가입 API")
    @PostMapping("/signUp")
    fun singUp(
        @RequestBody @Valid @ApiParam(value = "회원가입 정보", required = true)
        request: AccountRequestDto.SignUpRequest
    ): ResponseEntity<Account.AccountLoginSuccess> {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.singUp(request))
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
    @GetMapping("/currentPassword")
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

}

data class Test(
    @field: NotEmpty(message = "비밀번호를 입력해주세요")
    @field: CustomPassword
    val password: String,
)

fun main() {
    val test = Test(
        password = "1!wqdas2323"
    )
    val validatorFactory = Validation.buildDefaultValidatorFactory()
    val validator = validatorFactory.validator

    val constraints = validator.validate(test)
    constraints.forEach(System.out::println)
}