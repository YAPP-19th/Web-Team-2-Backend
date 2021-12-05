package com.yapp.web2.batch.job

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.notification.service.NotificationService
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.support.ListItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableBatchProcessing
class NotificationConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val notificationService: NotificationService
) {
    @Bean(name = arrayOf("bookmarkNotificationJob"))
    fun bookmarkNotificationJob(): Job {
        return jobBuilderFactory.get("bookmarkNotificationJob")
            .start(bookmarkNotificationStep())
            .incrementer(RunIdIncrementer())
            .build()
    }

    @Bean
    fun bookmarkNotificationStep(): Step {
        return stepBuilderFactory.get("bookmarkNotificationStep")
            .chunk<Bookmark, Bookmark>(10)
            .reader(remindBookmarkReader())
            .processor(remindBookmarkProcessor())
            .writer(remindBookmarkWriter())
            .build()
    }

    @Bean
    @StepScope
    fun remindBookmarkReader(): ListItemReader<Bookmark> {
        val bookmarkOfList = notificationService.getRemindBookmark()
        return ListItemReader(bookmarkOfList)
    }

    @Bean
    fun remindBookmarkProcessor(): ItemProcessor<Bookmark, Bookmark> {
        return ItemProcessor {
            notificationService.sendNotification(it.userId)
            it
        }
    }

    fun remindBookmarkWriter(): ItemWriter<Bookmark> {
        return ItemWriter {
            println("Complete Bookmark Chunk")
        }
    }
}