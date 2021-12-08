package com.yapp.web2.domain.account.controller

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.service.UserService
import com.yapp.web2.security.jwt.TokenDto
import com.yapp.web2.util.Message
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val userService: UserService
) {
    @ApiOperation(value = "로그인 & 토큰 발급")
    @PostMapping("/oauth2Login")
    fun oauth2Login(
        @RequestBody @ApiParam(value = "회원 정보", required = true) request: Account.AccountLoginRequest
    ): ResponseEntity<TokenDto> {
        val tokenDto = userService.oauth2LoginUser(request)
        return ResponseEntity.status(HttpStatus.OK).body(tokenDto)
    }

    @GetMapping("/test")
    fun test(): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation(value = "토큰 재발급")
    @GetMapping("/reIssuanceAccessToken")
    fun reIssuanceAccessToken(request: HttpServletRequest): ResponseEntity<TokenDto> {
        val accessToken = request.getHeader("AccessToken")
        val refreshToken = request.getHeader("RefreshToken")
        val tokenDto = userService.reIssuedAccessToken(accessToken, refreshToken)
        return ResponseEntity.status(HttpStatus.OK).body(tokenDto)
    }
}