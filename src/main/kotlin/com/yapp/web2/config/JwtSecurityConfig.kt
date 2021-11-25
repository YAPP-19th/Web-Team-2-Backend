package com.yapp.web2.config

import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.security.jwt.TokenAuthenticationFilter
import org.springframework.security.config.annotation.SecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

class JwtSecurityConfig(
    private val jwtProvider: JwtProvider
) : SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {

    override fun configure(http: HttpSecurity?) {
        val tokenAuthenticationFilter = TokenAuthenticationFilter(jwtProvider)
        http!!.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter()::class.java)
    }
}