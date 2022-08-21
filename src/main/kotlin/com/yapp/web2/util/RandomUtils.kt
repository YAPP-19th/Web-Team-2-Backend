package com.yapp.web2.util

import org.apache.commons.lang3.RandomStringUtils

class RandomUtils {

    companion object {

        fun getRandomNumber(from: Int, to: Int): Int {
            return (from..to).random()
        }

        fun getRandomAlphanumeric(count: Int): String {
            return RandomStringUtils.randomAlphanumeric(count)
        }

        fun getRandomSpecialCharacter(): String {
            val characters = "!@#$%^&*()"

            return characters.random().toString()
        }

        fun shuffleCharacters(words: String): String {
            return words.toList()
                .shuffled()
                .joinToString()
                .replace(", ", "")
        }
    }

}