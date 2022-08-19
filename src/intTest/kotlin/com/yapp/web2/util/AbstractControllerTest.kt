package com.yapp.web2.util

import com.ninjasquad.springmockk.MockkBean
import com.yapp.web2.config.S3Client
import com.yapp.web2.domain.ControllerTestUtil
import com.yapp.web2.domain.account.controller.AccountController
import com.yapp.web2.domain.account.service.AccountService
import com.yapp.web2.domain.folder.controller.FolderController
import com.yapp.web2.domain.folder.service.FolderService
import com.yapp.web2.security.jwt.JwtProvider
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter
import java.nio.charset.StandardCharsets

@WebMvcTest(
    value = [AccountController::class, FolderController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = ["extension.version=4.0.3"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractControllerTest {

    @Autowired
    internal lateinit var mockMvc: MockMvc

    @MockkBean
    internal lateinit var folderService: FolderService

    @MockkBean
    private lateinit var jwtProvider: JwtProvider

    @Autowired
    internal lateinit var context: WebApplicationContext

    @MockkBean
    internal lateinit var accountService: AccountService

    @MockkBean
    internal lateinit var s3Client: S3Client

    internal val util = ControllerTestUtil()

    @Value("\${extension.version}")
    internal lateinit var extensionVersion: String

    @BeforeAll
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .addFilters<DefaultMockMvcBuilder>(CharacterEncodingFilter(StandardCharsets.UTF_8.toString(), true))
            .alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
            .build()
    }

}