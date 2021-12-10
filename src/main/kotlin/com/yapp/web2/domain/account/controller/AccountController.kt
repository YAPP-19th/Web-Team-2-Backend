package com.yapp.web2.domain.account.controller

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.service.AccountService
import com.yapp.web2.security.jwt.TokenDto
import com.yapp.web2.util.Message
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/v1/user")
class AccountController(
    private val accountService: AccountService
) {
    @PostMapping("/oauth2Login")
    fun oauth2Login(
        @RequestBody @ApiParam(value = "회원 정보", required = true) request: Account.AccountLoginRequest
    ): ResponseEntity<Account.AccountLoginSuccess> {
        val loginSuccess = accountService.oauth2LoginUser(request)
        return ResponseEntity.status(HttpStatus.OK).body(loginSuccess)
    }

    @ApiOperation(value = "토큰 재발급")
    @GetMapping("/reIssuanceAccessToken")
    fun reIssuanceAccessToken(request: HttpServletRequest): ResponseEntity<TokenDto> {
        val accessToken = request.getHeader("AccessToken")
        val refreshToken = request.getHeader("RefreshToken")
        val tokenDto = accountService.reIssuedAccessToken(accessToken, refreshToken)
        return ResponseEntity.status(HttpStatus.OK).body(tokenDto)
    }

    @PostMapping("/changeProfileImage")
    fun changeProfileImage(request: HttpServletRequest, @RequestBody image: MultipartFile): ResponseEntity<String> {
        val token = request.getHeader("AccessToken")
        val image = accountService.changeProfileImage(token, image)
        return ResponseEntity.status(HttpStatus.OK).body(image)
    }

    @PostMapping("/nickNameCheck")
    fun nickNameCheck(@RequestBody nickName: Account.nextNickName): ResponseEntity<String> {
        val result = accountService.checkNickNameDuplication(nickName)
        return ResponseEntity.status(HttpStatus.OK).body(result)
    }
    @PostMapping("/nickNameChange")
    fun nickNameChange(request: HttpServletRequest, @RequestBody nickName: Account.nextNickName): ResponseEntity<String> {
        val token = request.getHeader("AccessToken")
        accountService.changeNickName(token, nickName)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @PostMapping("/changeBackgroundColor")
    fun changeBackgroundColor(request: HttpServletRequest, changeUrl: String): ResponseEntity<String> {
        val token = request.getHeader("AccessToken")
        accountService.changeBackgroundColor(token, changeUrl)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

}