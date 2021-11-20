package com.yapp.web2.config

import com.yapp.web2.security.oauth2.CustomOAuth2UserService
import com.yapp.web2.security.oauth2.utils.OAuth2FailureHandler
import com.yapp.web2.security.oauth2.utils.OAuth2SuccessHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Autowired private val customOAuth2UserService: CustomOAuth2UserService,
    @Autowired private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    @Autowired private val oAuth2FailureHandler: OAuth2FailureHandler
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        http!!.csrf().disable()

        http.formLogin()

        http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        http.authorizeRequests()
            .anyRequest().authenticated()

        http.oauth2Login()
            .userInfoEndpoint()
            .userService(customOAuth2UserService)
            .and()
            .successHandler(oAuth2SuccessHandler)
            .failureHandler(oAuth2FailureHandler)
    }

}