package com.yapp.web2.config

import com.yapp.web2.security.jwt.JwtProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.firewall.DefaultHttpFirewall
import org.springframework.security.web.firewall.HttpFirewall

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtProvider: JwtProvider
) : WebSecurityConfigurerAdapter() {

    @Bean
    fun getPasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    // request rejected 에러 로그 제거
    @Bean
    fun defaultHttpFirewall(): HttpFirewall {
        return DefaultHttpFirewall()
    }

    override fun configure(web: WebSecurity?) {
        web!!.httpFirewall(defaultHttpFirewall())
            .ignoring()
            .antMatchers("/api/v1/user/oauth2Login", "/api/v1/user/signIn", "/api/v1/user/signUp", "/api/v1/user/signUp/emailCheck")
            .antMatchers("/api/v1/user/reIssuanceAccessToken")
            .antMatchers("/api/v1/page/open/**", "/api/v1/page/open/**", "/api/v1/folder/encrypt/**")
            .antMatchers("/swagger-resources/**", "/v3/api-docs/**", "/swagger-ui/**")
    }

    override fun configure(http: HttpSecurity?) {
        http!!.csrf().disable().cors()

        http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        http.authorizeRequests()
            .antMatchers("/actuator/**").permitAll()
//            .antMatchers("/api/v1/page/open/**").permitAll()
            .anyRequest().authenticated()

        http.apply(JwtSecurityConfig(jwtProvider))
    }
}