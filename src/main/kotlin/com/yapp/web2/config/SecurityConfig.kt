package com.yapp.web2.config

import com.yapp.web2.security.jwt.JwtProvider
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtProvider: JwtProvider
) : WebSecurityConfigurerAdapter() {

    override fun configure(web: WebSecurity?) {
        web!!.ignoring()
            .antMatchers("/api/v1/user/oauth2Login", "/api/v1/user/reIssuanceAccessToken")
            .antMatchers("/v2/**", "/configuration/**", "/swagger*/**", "/webjars/**", "/swagger-resources/**")
            .antMatchers("/v3/**", "/swagger-ui", "/swagger-ui/**")
            .antMatchers("/actuator/**")
    }

    override fun configure(http: HttpSecurity?) {
        http!!.csrf().disable().cors()

        http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        http.authorizeRequests()
            .antMatchers("/api/v1/user/oauth2Login", "/api/v1/user/reIssuanceAccessToken").permitAll()
            .antMatchers("/v2/**", "/configuration/**", "/swagger*/**", "/webjars/**", "/swagger-resources/**").permitAll()
            .antMatchers("/v3/**", "/swagger-ui", "/swagger-ui/**").permitAll()
            .antMatchers("/actuator/**").permitAll()
            .anyRequest().authenticated()

        http.apply(JwtSecurityConfig(jwtProvider))
    }
}