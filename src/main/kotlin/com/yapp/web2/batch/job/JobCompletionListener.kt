package com.yapp.web2.batch.job

import com.yapp.web2.infra.slack.SlackServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.listener.JobExecutionListenerSupport
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class JobCompletionListener : JobExecutionListenerSupport() {

    private val log = LoggerFactory.getLogger(javaClass)
    private val slackApi: SlackServiceImpl = SlackServiceImpl()

    override fun afterJob(jobExecution: JobExecution) {
        val jobStatus = jobExecution.status
        log.info("Job {} - {}", jobStatus, jobExecution.jobInstance.jobName)

        if (jobStatus != BatchStatus.COMPLETED) {
            val errorMessage = String.format(
                "*`Batch Error`* - %s", getErrorMessage(jobExecution).replace("\"", "")
            )
            slackApi.sendMessage(errorMessage)
        }
    }

    fun getErrorMessage(jobExecution: JobExecution): String {
        return jobExecution.stepExecutions.stream()
            .map { it.failureExceptions.toString() }
            .collect(Collectors.joining(", "))
    }
}