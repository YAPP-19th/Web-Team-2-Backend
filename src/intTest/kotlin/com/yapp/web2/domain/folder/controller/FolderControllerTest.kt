package com.yapp.web2.domain.folder.controller

import com.yapp.web2.domain.BaseTimeEntity
import com.yapp.web2.domain.account.controller.AccountController
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.util.AbstractControllerTest
import com.yapp.web2.util.FolderTokenDto
import com.yapp.web2.util.Message
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.junit.jupiter.api.DisplayName
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
internal class FolderControllerTest : AbstractControllerTest() {

    @Test
    fun `폴더를 생성한다`() {
        // given
        val createFolderRequest = Folder.FolderCreateRequest(0L, "폴더")
        val folder: BaseTimeEntity = Folder("New Folder", 1, 0, null)
        folder.id = 1L
        every { folderService.createFolder(any(), any()) } returns folder as Folder

        // when
        val resultAction = util.postResultAction("/api/v1/folder", createFolderRequest, mockMvc)

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
        val resultAction = util.postResultAction("/api/v1/folder", createFolderRequest, mockMvc)

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
        val resultAction = util.patchResultAction("/api/v1/folder/1", changeFolderRequest, mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string(Message.SUCCESS))
    }

    @Test
    fun `폴더를 이동한다(드래그 & 드랍)`() {
        // given
        val moveFolderRequest = Folder.FolderMoveRequest(2L, 0)
        /* just Runs vs returns Unit 의 차이점은 뭘까? */
        every { folderService.moveFolderByDragAndDrop(any(), any(), any()) } returns Unit

        // when
        val resultAction = util.patchResultAction("/api/v1/folder/3/move", moveFolderRequest, mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string(Message.SUCCESS))
    }

    @Test
    fun `폴더를 이동한다(버튼)`() {
        // given
        val moveFolderButtonRequest = Folder.FolderMoveButtonRequest(mutableListOf(1L), 1L)
        every { folderService.moveFolderByButton(any(), any()) } just Runs

        // when
        val resultAction = util.patchResultAction("/api/v1/folder/move", moveFolderButtonRequest, mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string(Message.SUCCESS))
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
        val resultAction = util.deleteResultAction("/api/v1/folder/1", mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string(Message.SUCCESS))
    }

    @Test
    fun `폴더를 조회한다`() {
        // given
        val folders: Map<String, Any> = HashMap()
        every { folderService.findAll(any()) } returns folders

        // when
        val resultAction = util.getResultAction("/api/v1/folder", mockMvc)

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
        val resultAction = util.postResultAction("/api/v1/folder/deletes", deleteFolderListRequest, mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string(Message.SUCCESS))
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
        val resultAction = util.getResultAction("/api/v1/folder/1/children", mockMvc)

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
        val resultAction = util.getResultAction("/api/v1/folder/1/parent", mockMvc)

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

    @Test
    fun `암호화된 폴더 ID를 조회한다`() {
        // given
        val expected = "AES256token"
        every { folderService.encryptFolderId(any()) } returns FolderTokenDto(expected)

        // when
        val resultAction = util.getResultAction("/api/v1/folder/encrypt/1", mockMvc)

        // then
        resultAction
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.folderIdToken").value(expected))
    }
}