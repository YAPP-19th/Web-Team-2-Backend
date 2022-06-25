package com.yapp.web2.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.nio.charset.StandardCharsets

class ControllerTestUtil {

    fun getResultAction(uri: String, mockMvc: MockMvc): ResultActions {
        return mockMvc.perform(
            MockMvcRequestBuilders.get(uri)
                .header("AccessToken", "token")
                .header("RefreshToken", "retoken")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.toString())
        )
    }

    fun postResultAction(uri: String, request: Any, mockMvc: MockMvc): ResultActions {
        return mockMvc.perform(
            MockMvcRequestBuilders.post(uri)
                .content(jacksonObjectMapper().writeValueAsString(request))
                .header("AccessToken", "token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
    }

    fun patchResultAction(uri: String, request: Any, mockMvc: MockMvc): ResultActions {
        return mockMvc.perform(
            MockMvcRequestBuilders.patch(uri)
                .content(jacksonObjectMapper().writeValueAsString(request))
                .header("AccessToken", "token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
    }

    fun deleteResultAction(uri: String, mockMvc: MockMvc): ResultActions {
        return mockMvc.perform(
            MockMvcRequestBuilders.delete(uri)
                .header("AccessToken", "token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
    }

}