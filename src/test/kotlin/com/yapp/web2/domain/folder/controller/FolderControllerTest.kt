package com.yapp.web2.domain.folder.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.yapp.web2.domain.BaseTimeEntity
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.service.FolderService
import com.yapp.web2.security.jwt.JwtProvider
import com.yapp.web2.util.Message
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(FolderController::class, excludeAutoConfiguration = [SecurityAutoConfiguration::class])
@AutoConfigureMockMvc(addFilters = false)
internal class FolderControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var folderService: FolderService

    @MockkBean
    private lateinit var jwtProvider: JwtProvider

    @Test
    fun `폴더를 생성한다`() {
        // given
        val createFolderRequest = Folder.FolderCreateRequest(0L, "폴더")
        val folder: BaseTimeEntity = Folder("New Folder", 1, 0, null)
        folder.id = 1L
        every { folderService.createFolder(any(), any()) } returns folder as Folder

        // when
        val resultAction = postResultAction("/api/v1/folder", createFolderRequest)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.folderId").value(1L))
    }

    @Test
    fun `folderId가 null일 경우 아무것도 반환하지 않는다`() {
        // given
        val createFolderRequest = Folder.FolderCreateRequest(0L, "폴더")
        val folder: BaseTimeEntity = Folder("New Folder", 1, 0, null)
        every { folderService.createFolder(any(), any()) } returns folder as Folder

        // when
        val resultAction = postResultAction("/api/v1/folder", createFolderRequest)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").doesNotExist())
    }

    @Test
    fun `폴더를 수정한다`() {
        // given
        val changeFolderRequest = Folder.FolderChangeRequest("U+1F604", "Change Folder Name")
        every { folderService.changeFolder(any(), any()) } just Runs

        // when
        val resultAction = patchResultAction("/api/v1/folder/1", changeFolderRequest)
        val response = getResponseBody(resultAction)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
        assertThat(response).isEqualTo(Message.SUCCESS)
    }

    @Test
    fun `폴더를 이동한다(드래그 & 드랍)`() {
        // given
        val moveFolderRequest = Folder.FolderMoveRequest(2L, 0)
        /* just Runs vs returns Unit 의 차이점은 뭘까? */
        every { folderService.moveFolderByDragAndDrop(any(), any(), any()) } returns Unit

        // when
        val resultAction = patchResultAction("/api/v1/folder/3/move", moveFolderRequest)
        val response = getResponseBody(resultAction)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
        assertThat(response).isEqualTo(Message.SUCCESS)
    }

    @Test
    fun `폴더를 이동한다(버튼)`() {
        // given
        val moveFolderButtonRequest = Folder.FolderMoveButtonRequest(mutableListOf(1L), 1L)
        every { folderService.moveFolderByButton(any(), any()) } just Runs

        // when
        val resultAction = patchResultAction("/api/v1/folder/move", moveFolderButtonRequest)
        val response = getResponseBody(resultAction)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
        assertThat(response).isEqualTo(Message.SUCCESS)
    }

    @DisplayName("폴더의 경우 Hard Delete, 북마크의 경우 Soft Delete")
    @Test
    fun `폴더와 내부에 존재하는 모든 북마크를 삭제한다`() {
        // given
        val folder = Folder("New Folder", 1, 4, null)

        every { folderService.findByFolderId(any()) } returns folder
        every { folderService.deleteFolderRecursive(any()) } just Runs
        every { folderService.deleteFolder(any()) } just Runs

        // when
        val resultAction = deleteResultAction("/api/v1/folder/1")
        val response = getResponseBody(resultAction)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
        assertThat(response).isEqualTo(Message.SUCCESS)
    }

    @Test
    fun `폴더를 조회한다`() {
        // given
        val folders: Map<String, Any> = HashMap()
        every { folderService.findAll(any()) } returns folders

        // when
        val resultAction = getResultAction("/api/v1/folder")

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
    }

    @Test
    fun `폴더 리스트를 영구 삭제한다`() {
        // given
        val deleteFolderListRequest = Folder.FolderListDeleteRequest(mutableListOf(1L, 4L, 5L))
        every { folderService.deleteFolderList(any()) } just Runs

        // when
        val resultAction = postResultAction("/api/v1/folder/deletes", deleteFolderListRequest)
        val response = getResponseBody(resultAction)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
        assertThat(response).isEqualTo(Message.SUCCESS)
    }

    @Test
    fun `자식 폴더의 리스트를 조회한다`() {
        // given
        val folderList = mutableListOf(
            Folder.FolderListResponse(1L, "emoji1", "child1"),
            Folder.FolderListResponse(2L, "emoji2", "child2")
        )
        every { folderService.findFolderChildList(any()) } returns folderList

        // when
        val resultAction = getResultAction("/api/v1/folder/1/children")

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].folderId").value(1L))
            .andExpect(jsonPath("$[0].emoji").value("emoji1"))
            .andExpect(jsonPath("$[0].name").value("child1"))
            .andExpect(jsonPath("$[1].folderId").value(2L))
            .andExpect(jsonPath("$[1].emoji").value("emoji2"))
            .andExpect(jsonPath("$[1].name").value("child2"))
    }

    @Test
    fun `부모 폴더의 리스트를 조회한다`() {
        // given
        val folderList = mutableListOf(
            Folder.FolderListResponse(1L, "emoji1", "parent1"),
            Folder.FolderListResponse(2L, "emoji2", "parent2")
        )
        every { folderService.findAllParentFolderList(any()) } returns folderList

        // when
        val resultAction = getResultAction("/api/v1/folder/1/parent")

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].folderId").value(1L))
            .andExpect(jsonPath("$[0].emoji").value("emoji1"))
            .andExpect(jsonPath("$[0].name").value("parent1"))
            .andExpect(jsonPath("$[1].folderId").value(2L))
            .andExpect(jsonPath("$[1].emoji").value("emoji2"))
            .andExpect(jsonPath("$[1].name").value("parent2"))
    }

    private fun getResultAction(uri: String): ResultActions {
        return mockMvc.perform(
            MockMvcRequestBuilders.get(uri)
                .header("AccessToken", "token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
    }

    private fun postResultAction(uri: String, request: Any): ResultActions {
        return mockMvc.perform(
            MockMvcRequestBuilders.post(uri)
                .content(jacksonObjectMapper().writeValueAsString(request))
                .header("AccessToken", "token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
    }

    private fun patchResultAction(uri: String, request: Any): ResultActions {
        return mockMvc.perform(
            MockMvcRequestBuilders.patch(uri)
                .content(jacksonObjectMapper().writeValueAsString(request))
                .header("AccessToken", "token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
    }

    private fun deleteResultAction(uri: String): ResultActions {
        return mockMvc.perform(
            MockMvcRequestBuilders.delete(uri)
                .header("AccessToken", "token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
    }

    private fun getResponseBody(resultActions: ResultActions): String {
        return resultActions.andReturn().response.contentAsString
    }
}