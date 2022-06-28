package com.yapp.web2.domain.account.controller

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.entity.AccountRequestDto
import com.yapp.web2.domain.folder.controller.FolderController
import com.yapp.web2.security.jwt.TokenDto
import com.yapp.web2.util.AbstractControllerTest
import com.yapp.web2.util.Message
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(
    value = [AccountController::class, FolderController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = ["extension.version=4.0.3"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountControllerTest: AbstractControllerTest() {

    @Test
    fun `현재 회원의 프로필을 조회한다`() {
        // given
        val accountProfile = Account.AccountProfile(
            "a@a.com", "Nickname", "https://s3-test.com", "google", "FCMToken"
        )
        every { accountService.getProfile(any()) } returns accountProfile

        // when
        val resultAction = util.getResultAction("/api/v1/user/profileInfo", mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value(accountProfile.email))
            .andExpect(jsonPath("$.name").value(accountProfile.name))
            .andExpect(jsonPath("$.image").value(accountProfile.image))
            .andExpect(jsonPath("$.socialType").value(accountProfile.socialType))
            .andExpect(jsonPath("$.fcmToken").value(accountProfile.fcmToken))
    }

    @Test
    fun `현재 회원의 리마인드 정보를 조회한다`() {
        // given
        val remindElements = Account.RemindElements(7, true)
        every { accountService.getRemindElements(any()) } returns remindElements

        // when
        val resultAction = util.getResultAction("/api/v1/user/remindInfo", mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.remindCycle").value(remindElements.remindCycle))
            .andExpect(jsonPath("$.remindToggle").value(remindElements.remindToggle))
    }

    @Test
    fun `소셜 로그인을 한다`() {
        // given
        val accountProfile = Account.AccountProfile(
            "a@a.com", "Nickname", "https://s3-test.com", "google", "FCMToken"
        )
        val accountLoginSuccess = Account.AccountLoginSuccess(
            TokenDto("AccessToken", "RefreshToken"),
            Account("a@a.com"),
            false
        )
        every { accountService.oauth2LoginUser(any()) } returns accountLoginSuccess

        // when
        val resultAction = util.postResultAction("/api/v1/user/oauth2Login", accountProfile, mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isRegistered").value(accountLoginSuccess.isRegistered))
            .andExpect(jsonPath("$.email").value(accountLoginSuccess.email))
    }

    @Test
    fun `토큰을 재발급 한다`() {
        // given
        val tokenDto = TokenDto("Re-AccessToken", "Re-RefreshToken")
        every { accountService.reIssuedAccessToken(any(), any()) } returns tokenDto

        // when
        val resultAction = util.getResultAction("/api/v1/user/reIssuanceAccessToken", mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value(tokenDto.accessToken))
            .andExpect(jsonPath("$.refreshToken").value(tokenDto.refreshToken))
    }

    @Test
    fun `현재 회원의 프로필 이미지를 변경한다`() {
        // TODO: 2022/05/18
    }

    @Test
    fun `현재 회원의 프로필을 편집한다`() {
        // given
        val request = Account.ProfileChanged("change image", "change name")
        every { accountService.changeProfile(any(), any()) } just Runs

        // when
        val resultAction = util.postResultAction("/api/v1/user/changeProfile", request, mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string(Message.SUCCESS))
    }

    @Test
    fun `현재 회원의 닉네임을 비교한다`() {
        // given
        val request = Account.NextNickName("Nickname")
        every { accountService.checkNickNameDuplication(any(), any()) } returns Message.AVAILABLE_NAME

        // when
        val resultAction = util.postResultAction("/api/v1/user/nickNameCheck", request, mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string(Message.AVAILABLE_NAME))
    }

    @Test
    fun `현재 회원의 닉네임을 변경한다`() {
        // given
        val request = Account.NextNickName("Nickname")
        every { accountService.changeNickName(any(), any()) } just Runs

        // when
        val resultAction = util.postResultAction("/api/v1/user/nickNameChange", request, mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string(Message.SUCCESS))
    }

    @Test
    fun `현재 회원의 배경 색상을 변경한다`() {
        // TODO: 2022/05/18
    }

    @Test
    fun `현재 회원의 익스텐션 버전을 조회한다`() {
        // given
        every { accountService.checkExtension(any()) } returns extensionVersion
        println("exten: $extensionVersion")

        // when
        val resultAction = util.getResultAction("/api/v1/user/$extensionVersion", mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string(extensionVersion))
    }

    @Test
    fun `일반 회원가입을 진행한다`() {
        // given
        val request = AccountRequestDto.SignUpRequest("a@a.com", "1234567!", "token")
        val accountLoginSuccess = Account.AccountLoginSuccess(
            TokenDto("AccessToken", "RefreshToken"),
            Account("a@a.com"),
            false
        )
        every { accountService.signUp(any()) } returns accountLoginSuccess

        // when
        val resultAction = util.postResultAction("/api/v1/user/signUp", request, mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isRegistered").value(accountLoginSuccess.isRegistered))
            .andExpect(jsonPath("$.email").value(accountLoginSuccess.email))
    }

    @Test
    fun `일반 로그인에 성공한다`() {
        // given
        val request = AccountRequestDto.SignInRequest("a@a.com", "1234567!")
        val accountLoginSuccess = Account.AccountLoginSuccess(
            TokenDto("AccessToken", "RefreshToken"),
            Account("a@a.com"),
            false
        )
        every { accountService.signIn(any()) } returns accountLoginSuccess

        // when
        val resultAction = util.postResultAction("/api/v1/user/signIn", request, mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isRegistered").value(accountLoginSuccess.isRegistered))
            .andExpect(jsonPath("$.email").value(accountLoginSuccess.email))
    }

    @Test
    fun `현재 비밀번호와 입력받은 비밀번호가 동일하면 성공한다`() {
        // given
        val request = AccountRequestDto.CurrentPassword("1234567!")
        every { accountService.comparePassword(any(), any()) } returns Message.SAME_PASSWORD

        // when
        val resultAction = util.postResultAction("/api/v1/user/passwordCheck", request, mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string(Message.SAME_PASSWORD))
    }

    @Test
    fun `비밀번호를 정상적으로 변경한다`() {
        // given
        val request = AccountRequestDto.PasswordChangeRequest("before1!", "after123!")
        every { accountService.changePassword(any(), any()) } returns Message.CHANGE_PASSWORD_SUCCEED

        // when
        val resultAction = util.patchResultAction("/api/v1/user/password", request, mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string(Message.CHANGE_PASSWORD_SUCCEED))
    }

    @Test
    fun `회원을 정상적으로 탈퇴한다`() {
        // given
        every { accountService.softDelete(any()) } just Runs

        // when
        val resultAction = util.deleteResultAction("/api/v1/user/unregister", mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string(Message.DELETE_ACCOUNT_SUCCEED))
    }

    @Test
    fun `비밀번호 재설정 시 이메일이 존재하는지 확인한다`() {
        // given
        val request = AccountRequestDto.EmailCheckRequest("a@a.com")
        every { accountService.checkEmailExist(any(), any()) } returns Message.SUCCESS_EXIST_EMAIL

        // when
        val resultAction = util.postResultAction("/api/v1/user/password/emailCheck", request, mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string(Message.SUCCESS_EXIST_EMAIL))
    }

    @Test
    fun `비밀번호 재설정 시 임시 비밀번호를 생성하고 메일을 발송한다`() {
        // given
        val tempPassword = RandomStringUtils.randomAlphanumeric(12) + "!"
        every { accountService.createTempPassword() } returns tempPassword
        every { accountService.updatePassword(any(), any()) } just Runs
        every { accountService.sendMail(any(), tempPassword) } returns Message.SUCCESS_SEND_MAIL

        // when
        val resultAction = util.postResultAction("/api/v1/user/password/reset", "", mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string(Message.SUCCESS_SEND_MAIL))
    }

}
