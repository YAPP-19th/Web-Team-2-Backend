package com.yapp.web2.domain.account.controller

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.service.AccountService
import com.yapp.web2.security.jwt.TokenDto
import com.yapp.web2.util.Message
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
        @RequestBody request: Account.AccountLoginRequest
    ): ResponseEntity<TokenDto> {
        val tokenDto = accountService.oauth2LoginUser(request)
        return ResponseEntity.status(HttpStatus.OK).body(tokenDto)
    }

    @GetMapping("/reIssuanceAccessToken")
    fun reIssuanceAccessToken(request: HttpServletRequest): ResponseEntity<TokenDto> {
        val accessToken = request.getHeader("AccessToken")
        val refreshToken = request.getHeader("RefreshToken")
        val tokenDto = accountService.reIssuedAccessToken(accessToken, refreshToken)
        return ResponseEntity.status(HttpStatus.OK).body(tokenDto)
    }

    @PostMapping("/changeProfileImage")
    fun upload(request: HttpServletRequest, @RequestParam("images") multipartFile: MultipartFile): ResponseEntity<String> {
        val token = request.getHeader("AccessToken")
        val image = accountService.changeProfileImage(token, multipartFile)
        return ResponseEntity.status(HttpStatus.OK).body(image)
    }
}