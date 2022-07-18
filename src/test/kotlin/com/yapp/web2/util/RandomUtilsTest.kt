package com.yapp.web2.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class RandomUtilsTest {

    @RepeatedTest(5)
    fun `0 ~ 9 사이의 랜덤 숫자를 생성한다`() {
        val randomNumber = RandomUtils.getRandomNumber(0, 9)

        assertTrue(randomNumber in 0..9)
    }

    @RepeatedTest(5)
    fun `12자리의 랜덤 문자열을 생성한다`() {
        val randomString = RandomUtils.getRandomAlphanumeric(12)

        assertThat(12).isEqualTo(randomString.length)
    }

    @Test
    fun `임의의 특수문자를 생성한다`() {
        val characters = "!@#$%^&*()"
        val randomSpecialCharacter = RandomUtils.getRandomSpecialCharacter()

        assertAll(
            { assertTrue(characters.contains(randomSpecialCharacter)) },
            { assertThat(randomSpecialCharacter.length).isEqualTo(1) }
        )
    }

    @Test
    fun `문자열을 랜덤으로 섞는다`() {
        val words = "fjdkDzcm3a!mckz"

        val shuffledWords = RandomUtils.shuffleCharacters(words)

        assertAll(
            { assertThat(words.length).isEqualTo(shuffledWords.length) },
            { assertThat(words.toSortedSet()).isEqualTo(shuffledWords.toSortedSet()) }
        )
    }

}


