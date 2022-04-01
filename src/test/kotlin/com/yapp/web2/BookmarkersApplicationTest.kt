package com.yapp.web2

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("dev")
@SpringBootTest
internal class BookmarkersApplicationTest {

    @Value("\${cloud.aws.region.static}")
    lateinit var actualRegion: String

    @Test
    fun `application-dev properties 값을 확인한다`() {
        // given
        val expected = "ap-northeast-2"

        // then
        assertThat(actualRegion).isEqualTo(expected)
    }
}