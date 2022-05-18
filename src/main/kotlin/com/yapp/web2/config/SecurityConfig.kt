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

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtProvider: JwtProvider
) : WebSecurityConfigurerAdapter() {

    @Bean
    fun getPasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    override fun configure(web: WebSecurity?) {
        web!!.ignoring()
            .antMatchers("/api/v1/user/oauth2Login", "/api/v1/user/signUp", "/api/v1/user/reIssuanceAccessToken")
            .antMatchers("/swagger-resources/**", "/v3/api-docs/**", "/swagger-ui/**")
    }

    override fun configure(http: HttpSecurity?) {
        http!!.csrf().disable().cors()

        http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        http.authorizeRequests()
            .antMatchers("/api/v1/user/reIssuanceAccessToken").permitAll()
            .antMatchers("/actuator/**").permitAll()
            .antMatchers("/api/v1/page/open/**").permitAll()
            .anyRequest().authenticated()

        http.apply(JwtSecurityConfig(jwtProvider))
    }
}