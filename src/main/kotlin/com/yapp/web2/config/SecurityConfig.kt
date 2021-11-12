package com.yapp.web2.config

import com.yapp.web2.security.CustomOAuth2UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Autowired private val customOAuth2UserService: CustomOAuth2UserService
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        http!!.csrf().disable()
            .formLogin()
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .oauth2Login()
            .userInfoEndpoint()
            .userService(customOAuth2UserService)

//        http!!.csrf().disable()
//
//        http.formLogin()
//
//        http.sessionManagement()
//            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//
//        http.authorizeRequests()
//            .antMatchers("/auth/**", "/", "/loginSuccess", "/test").permitAll()
//            .anyRequest().authenticated()
//
//        http.oauth2Login()
//            .userInfoEndpoint()
//            .userService(customOAuth2UserService)

    }

}