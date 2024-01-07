package com.suihan74.satena2

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun urlRegex() {
        val expected = listOf("テスト ", "https://b.hatena.ne.jp/hogehoge", " testてすと")
        val input = expected.joinToString("")
        val urlRegex = Regex("""https?://([\w-]+\.)+[\w-]+(/[a-zA-Z0-9_\-+./!?%&=|^~#@*;:,<>()\[\]{}]*)?""")
        val result = buildList {
            var matcher = urlRegex.find(input, 0)
            if (matcher == null) {
                add(input)
            }
            else {
                var last = input
                while (matcher != null) {
                    add(last.substring(0 until matcher.range.first))
                    add(matcher.value)
                    last = last.substring(matcher.range.last + 1)
                    matcher = urlRegex.find(last)
                }
                if (last.isNotEmpty()) { add(last) }
            }
        }

        assertArrayEquals(expected.toTypedArray(), result.toTypedArray())
    }

    @Test
    fun tagsRegex() {
        val tagRegex = Regex("""\[([^%/:\[\]]+)]""")
        val tagsAreaRegex = Regex("""^(\[[^%/:\[\]]+])+""")
        val input = "[test1][t2][テ3]aaabbbccc"
        val tagsAreaMatchResult = tagsAreaRegex.find(input)
        tagsAreaMatchResult?.value?.let { tagsArea ->
            val tags = tagRegex.findAll(tagsArea)
            assertEquals(3, tags.count())
        } ?: run { assert(false) }
    }
}
