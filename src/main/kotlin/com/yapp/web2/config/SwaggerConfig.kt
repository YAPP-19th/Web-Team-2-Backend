package com.yapp.web2.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.ApiKey
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger.web.SecurityConfiguration
import springfox.documentation.swagger.web.SecurityConfigurationBuilder

@Configuration
@OpenAPIDefinition
class SwaggerConfig {

    @Bean
    fun security(): SecurityConfiguration? {
        return SecurityConfigurationBuilder.builder()
            .clientId(null)
            .clientSecret(null)
            .realm("realm")
            .appName("test-app")
            .scopeSeparator(",")
            .additionalQueryStringParams(null)
            .useBasicAuthenticationWithAccessCodeGrant(false)
            .build()
    }

    @Bean
    fun productApi(): Docket {
        return Docket(DocumentationType.OAS_30)
            .apiInfo(this.metaInfo())
            .securityContexts(listOf(securityContext()))
            .securitySchemes(listOf(apiKey()))
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(RestController::class.java))
            .paths(PathSelectors.any())
            .build()
    }

    fun securityContext(): SecurityContext {
        return SecurityContext.builder().securityReferences(defaultAuth()).build()
    }

    fun defaultAuth(): List<SecurityReference> {
        val authorizationScope = AuthorizationScope("global", "accessEverything")
        val authorizationScopes = arrayOfNulls<AuthorizationScope>(1)
        authorizationScopes[0] = authorizationScope
        return listOf(SecurityReference("JWT", authorizationScopes))
    }

    fun apiKey(): ApiKey {
        return ApiKey("JWT", "AccessToken", "header")
    }

    private fun metaInfo(): ApiInfo {
        return ApiInfoBuilder()
            .title("도토리함")
            .description("https://github.com/YAPP-19th/Web-Team-2-Frontend")
            .description("https://github.com/YAPP-19th/Web-Team-2-Backend")
            .version("v1.0")
            .license("© Apache")
            .licenseUrl("http://www.apache.org/licenses/")
            .build()
    }
}