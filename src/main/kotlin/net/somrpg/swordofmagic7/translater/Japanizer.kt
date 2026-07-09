/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2020
 *
 * Porting from Java to Kotlin
 */
package net.somrpg.swordofmagic7.translater

/**
 * ローマ字表記を漢字変換して返すユーティリティ
 */
object Japanizer {
    private val REGEX_URL = Regex("https?://[\\w/:%#$&?()~.=+\\-]+")

    /**
     * メッセージの日本語化をする
     */
    @JvmStatic
    fun japanize(
        org: String,
        dictionary: Map<String, String>,
    ): String {
        val deletedURL = org.replace(REGEX_URL, " ")

        val keywordMap = HashMap<String, String>()
        var index = 0
        var keywordLocked = deletedURL
        for (dickey in dictionary.keys) {
            if (keywordLocked.contains(dickey)) {
                index++
                val key = "＜${makeMultibytesDigit(index)}＞"
                keywordLocked = keywordLocked.replace(dickey, key)
                keywordMap[key] = dictionary[dickey]!!
            }
        }

        var japanized = YukiKanaConverter.conv(keywordLocked)
        japanized = IMEConverter.convByGoogleIME(japanized)

        for ((key, value) in keywordMap) {
            japanized = japanized.replace(key, value)
        }

        val result = japanized.trim()
        return result
    }

    /**
     * 日本語化が必要かどうかを判定する
     */
    @JvmStatic
    fun isNeedToJapanize(org: String): Boolean {
        val byteLength = org.toByteArray().size
        val charLength = org.length
        val isHalfWidthAscii = byteLength == charLength
        val isHalfWidthKatakanaOnly = org.matches(Regex("[ \\uFF61-\\uFF9F]+"))
        val result = isHalfWidthAscii && !isHalfWidthKatakanaOnly
        return result
    }

    private fun makeMultibytesDigit(digit: Int): String {
        val half = digit.toString()
        val result = StringBuilder()
        for (c in half) {
            result.append(
                when (c) {
                    '0' -> "０"
                    '1' -> "１"
                    '2' -> "２"
                    '3' -> "３"
                    '4' -> "４"
                    '5' -> "５"
                    '6' -> "６"
                    '7' -> "７"
                    '8' -> "８"
                    '9' -> "９"
                    else -> ""
                },
            )
        }
        return result.toString()
    }
}
