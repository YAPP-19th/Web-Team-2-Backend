package com.yapp.web2.batch.job

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import org.slf4j.LoggerFactory
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

    private val log = LoggerFactory.getLogger(TrashRefreshJob::class.java)

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
        val deleteBookmarkList = bookmarkRepository.findAllByDeletedIsTrueAndDeleteTimeBefore(
            LocalDateTime.now().minusDays(30)
        )
        log.info("휴지통에서 30일이 지난 북마크는 자동으로 삭제합니다. 삭제할 북마크 갯수: ${deleteBookmarkList.size}")

        return ListItemReader(deleteBookmarkList)
    }

    @Bean
    fun deleteBookmarkProcessor(): ItemProcessor<Bookmark, Bookmark> {
        return ItemProcessor {
            log.info("Bookmark to delete info => userId: ${it.userId}, folderId: ${it.folderId}, folderName: ${it.folderName} title: ${it.title}")
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