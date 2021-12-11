package com.yapp.web2.domain.notification.controller

import com.yapp.web2.domain.notification.entity.dto.RemindCycleRequest
import com.yapp.web2.domain.notification.entity.dto.RemindToggleRequest
import com.yapp.web2.domain.notification.service.NotificationService
import com.yapp.web2.util.Message
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/v1/mypage/remind")
class NotificationController(
    private val notificationService: NotificationService
) {

    @ApiOperation(value = "리마인드 알람 설정(토글) API")
    @PatchMapping("/toggle")
    fun changeRemindAlarm(
        servletRequest: HttpServletRequest,
        @RequestBody @ApiParam(value = "리마인드 토글(true / false)", required = true) request: RemindToggleRequest
    ): ResponseEntity<String> {
        val accessToken = servletRequest.getHeader("AccessToken")
        notificationService.changeRemindAlarm(request, accessToken)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }


    @ApiOperation(value = "리마인드 알람 주기 설정 API")
    @PostMapping("/cycle")
    fun updateRemindAlarmCycle(
        servletRequest: HttpServletRequest,
        @RequestBody @ApiParam(value = "리마인드 알람 주기 설정 정보", required = true) request: RemindCycleRequest
    ): ResponseEntity<String> {
        val accessToken = servletRequest.getHeader("AccessToken")
        notificationService.updateRemindAlarmCycle(request, accessToken)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }


}