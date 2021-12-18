package com.yapp.web2.domain.remind.controller

import com.yapp.web2.domain.remind.entity.dto.RemindCycleRequest
import com.yapp.web2.domain.remind.entity.dto.RemindToggleRequest
import com.yapp.web2.domain.remind.service.RemindService
import com.yapp.web2.util.Message
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/v1")
class RemindController(
    private val remindService: RemindService
) {

    @ApiOperation(value = "리마인드 알람 설정(토글) API")
    @PatchMapping("/mypage/remind/toggle")
    fun changeRemindToggle(
        servletRequest: HttpServletRequest,
        @RequestBody @ApiParam(value = "리마인드 토글(true / false)", required = true) request: RemindToggleRequest
    ): ResponseEntity<String> {
        val accessToken = servletRequest.getHeader("AccessToken")
        remindService.changeRemindAlarm(request, accessToken)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }


    @ApiOperation(value = "리마인드 알람 주기 설정 API")
    @PostMapping("/mypage/remind/cycle")
    fun updateRemindAlarmCycle(
        servletRequest: HttpServletRequest,
        @RequestBody @ApiParam(value = "리마인드 알람 주기 설정 정보", required = true) request: RemindCycleRequest
    ): ResponseEntity<String> {
        val accessToken = servletRequest.getHeader("AccessToken")
        remindService.updateRemindAlarmCycle(request, accessToken)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation(value = "리마인드 알림 삭제 API")
    @DeleteMapping("/remind/{bookmarkId}")
    fun bookmarkRemindOff(
        @PathVariable @ApiParam(value = "북마크 ID", required = true) bookmarkId: String
    ): ResponseEntity<String> {
        remindService.bookmarkRemindOff(bookmarkId)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

}