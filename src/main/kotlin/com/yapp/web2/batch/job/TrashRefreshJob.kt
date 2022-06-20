package com.yapp.web2.batch.job

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.support.ListItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime

@Configuration
@EnableBatchProcessing
class TrashRefreshJob(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val bookmarkRepository: BookmarkRepository,
    private val jobCompletionListener: JobCompletionListener
) {

    @Bean("bookmarkTrashRefreshJob")
    fun bookmarkTrashRefreshJob(): Job {
        return jobBuilderFactory.get("bookmarkTrashRefreshJob")
            .start(trashRefreshStep())
            .incrementer(RunIdIncrementer())
            .listener(jobCompletionListener)
            .build()
    }

    @Bean
    fun trashRefreshStep(): Step {
        return stepBuilderFactory.get("trashRefreshStep")
            .chunk<Bookmark, Bookmark>(10)
            .reader(deleteBookmarkReader())
            .processor(deleteBookmarkProcessor())
            .writer(NoOperationItemWriter())
            .build()
    }

    @Bean
    fun deleteBookmarkReader(): ListItemReader<Bookmark> {
        val deleteBookmarkList = bookmarkRepository.findAllByDeletedIsTrueAndDeleteTimeIsAfter(
            LocalDateTime.now().minusDays(30)
        )

        return ListItemReader(deleteBookmarkList)
    }

    @Bean
    fun deleteBookmarkProcessor(): ItemProcessor<Bookmark, Bookmark> {
        return ItemProcessor {
            bookmarkRepository.delete(it)
            it
        }
    }
}

class NoOperationItemWriter : ItemWriter<Bookmark> {
    override fun write(items: MutableList<out Bookmark>) {
        // no-operation
    }

}