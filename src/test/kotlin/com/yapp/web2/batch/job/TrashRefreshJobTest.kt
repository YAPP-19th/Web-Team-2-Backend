package com.yapp.web2.batch.job

import com.yapp.web2.batch.BatchTestConfig
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.jupiter.api.assertAll
import org.junit.runner.RunWith
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@SpringBatchTest
@SpringBootTest(classes = [
    TrashRefreshJob::class, BatchTestConfig::class, JobCompletionListener::class
])
@RunWith(SpringRunner::class)
@ActiveProfiles("dev")
@EnableMongoRepositories(basePackages = ["com.yapp.web2.domain.bookmark.repository"])
internal class TrashRefreshJobTest {

    @Autowired
    lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    lateinit var bookmarkRepository: BookmarkRepository

    @Test
    fun `bookmarkTrashRefreshJob을 정상적으로 수행한다`() {
        // given
        val expectedJobName = "bookmarkTrashRefreshJob"
        val expectedStepSize = 1

        // when
        val jobExecution = jobLauncherTestUtils.launchJob()

        // then
        assertAll(
            { assertThat(jobExecution.jobInstance.jobName).isEqualTo(expectedJobName) },
            { assertThat(jobExecution.exitStatus).isEqualTo(ExitStatus.COMPLETED) },
            { assertThat(jobExecution.stepExecutions.size).isEqualTo(expectedStepSize) }
        )
    }

}
