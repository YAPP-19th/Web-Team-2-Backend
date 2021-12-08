package com.yapp.web2.batch.scheduler

import com.yapp.web2.batch.job.NotificationConfig
import org.springframework.batch.core.JobParameter
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat

@Component
class RemindNotificationScheduler(
    private val jobLauncher: JobLauncher,
    private val notificationConfig: NotificationConfig
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    //초 분 시 일 월 요일
    // 시간을 jobParameter로 하여, 문제가 생겼을 때, 재시도하더라도 키가 다르게 해준다.
    // 배포할 때는 다른 방식의 접근이 필요하다.
    @Scheduled(cron = "0 0 13 * * *")
    fun remindBookmarkNotificationSchedule() {
        val jobConf = hashMapOf<String, JobParameter>()
        jobConf["time"] = JobParameter(dateFormat.format(System.currentTimeMillis()))
        val jobParameters = JobParameters(jobConf)

        jobLauncher.run(notificationConfig.bookmarkNotificationJob(), jobParameters)
    }
}