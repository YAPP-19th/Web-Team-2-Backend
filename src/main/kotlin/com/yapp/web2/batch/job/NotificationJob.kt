package com.yapp.web2.batch.job

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.remind.service.RemindService
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.support.ListItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableBatchProcessing
class NotificationJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val remindService: RemindService,
    private val jobCompletionListener: JobCompletionListener
) {

    companion object {
        const val SEND_NOTIFICATION_JOB = "SEND_NOTIFICATION_JOB"
        const val SEND_NOTIFICATION_STEP = "SEND_NOTIFICATION_STEP"
    }

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun bookmarkNotificationJob(): Job {
        return jobBuilderFactory.get(SEND_NOTIFICATION_JOB)
            .start(bookmarkNotificationStep())
            .incrementer(RunIdIncrementer())
            .listener(jobCompletionListener)
            .build()
    }

    @Bean
    @JobScope
    fun bookmarkNotificationStep(): Step {
        return stepBuilderFactory.get(SEND_NOTIFICATION_STEP)
            .chunk<Bookmark, Bookmark>(10)
            .reader(remindBookmarkReader())
            .processor(remindBookmarkProcessor())
            .writer(remindBookmarkWriter())
            .build()
    }

    // TODO: ListItemReader의 경우 모든 데이터를 읽고 메모리에 올려두고 사용하기에 데이터가 많을경우 OOM 발생할 가능성이 존재함
    @Bean
    @StepScope
    fun remindBookmarkReader(): ItemReader<Bookmark> {
        return ListItemReader(remindService.getRemindBookmark())
    }

    // TODO: Notification 실패 처리 -> Queue(Kafka) 적재 후 Retry 처리
    @Bean
    @StepScope
    fun remindBookmarkProcessor(): ItemProcessor<Bookmark, Bookmark> {
        return ItemProcessor {
            log.info("Bookmark id: ${it.id}, 리마인드 발송 갯수: ${it.remindList.size}")
            remindService.sendNotification(it)
            it
        }
    }

    @Bean
    @StepScope
    fun remindBookmarkWriter(): ItemWriter<Bookmark> {
        return ItemWriter {
            it.stream().forEach { bookmark ->
                bookmark.remindList.forEach { remind ->
                    remind.successRemind()
                }
                remindService.save(bookmark)
                log.info("${bookmark.userId} -> ${bookmark.title} Send Notification")
            }
        }
    }
}