package com.yapp.web2.batch.scheduler

import com.yapp.web2.batch.job.TrashRefreshJob
import org.springframework.batch.core.JobParameter
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat

@Component
class TrashRefreshScheduler(
    private val jobLauncher: JobLauncher,
    private val trashRefreshJob: TrashRefreshJob
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    @Scheduled(cron = "0 0 2 * * *")
    fun trashRefreshSchedule() {
        val jobConf = hashMapOf<String, JobParameter>()
        jobConf["time"] = JobParameter(dateFormat.format(System.currentTimeMillis()))
        val jobParameters = JobParameters(jobConf)

        jobLauncher.run(trashRefreshJob.bookmarkTrashRefreshJob(), jobParameters)
    }
}