package com.yapp.web2.domain.user.controller

import com.yapp.web2.domain.user.entity.Account
import com.yapp.web2.domain.user.service.UserService
import com.yapp.web2.security.jwt.TokenDto
import com.yapp.web2.util.Message
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val userService: UserService
) {
    @PostMapping("/oauth2Login")
    fun oauth2Login(
        @RequestBody request: Account.AccountRequest
    ): ResponseEntity<TokenDto> {
        val tokenDto = userService.oauth2LoginUser(request)
        return ResponseEntity.status(HttpStatus.OK).body(tokenDto)
    }

    @GetMapping("/test")
    fun test(): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @GetMapping("/reIssuanceAccessToken")
    fun reIssuanceAccessToken(request: HttpServletRequest): ResponseEntity<TokenDto> {
        val accessToken = request.getHeader("Access-Token")
        val refreshToken = request.getHeader("Refresh-Token")
        val tokenDto = userService.reIssuedAccessToken(accessToken, refreshToken)
        return ResponseEntity.status(HttpStatus.OK).body(tokenDto)
    }
}