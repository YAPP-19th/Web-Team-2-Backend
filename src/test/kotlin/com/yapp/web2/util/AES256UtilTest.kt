package com.yapp.web2.util

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class AES256UtilTest {

    private val aeS256Util = AES256Util("1234567891123412342345678")

    @Test
    fun `암호화가 제대로 되는지 확인한다`() {
        // given
        val testString = "testString"

        // when
        val actual = aeS256Util.encrypt(testString)

        // then
        println(actual)
        Assertions.assertThat(actual).isNotNull
    }

    @Test
    fun `복호화가 제대로 되는지 확인한다`() {
        //given
        val testString = "testString"
        val testEncryptString = aeS256Util.encrypt(testString)

        //when
        val actual = aeS256Util.decrypt(testEncryptString)

        //then
        Assertions.assertThat(actual).isEqualTo(testString)
    }
}
