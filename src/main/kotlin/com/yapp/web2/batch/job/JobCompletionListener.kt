package com.yapp.web2.batch.job

import com.yapp.web2.infra.slack.SlackService
import org.slf4j.LoggerFactory
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.listener.JobExecutionListenerSupport
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class JobCompletionListener(
    private val slackApi: SlackService
) : JobExecutionListenerSupport() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun afterJob(jobExecution: JobExecution) {
        val jobStatus = jobExecution.status
        log.info("Job {} - {}", jobStatus, jobExecution.jobInstance.jobName)

        if (jobStatus == BatchStatus.COMPLETED) {
            slackApi.sendSlackAlarm(
                "*`${jobExecution.jobInstance.jobName}`* - executed"
            )
        }

        if (jobStatus != BatchStatus.COMPLETED) {
            val errorMessage = String.format(
                "*`Batch Error`* - %s", getErrorMessage(jobExecution).replace("\"", "")
            )
            slackApi.sendSlackAlarm(errorMessage)
        }
    }

    fun getErrorMessage(jobExecution: JobExecution): String {
        return jobExecution.stepExecutions.stream()
            .map { it.failureExceptions.toString() }
            .collect(Collectors.joining(", "))
    }
}