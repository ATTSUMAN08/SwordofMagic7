package net.somrpg.swordofmagic7.translater

object Japanizer {

    private const val REGEX_URL = "https?://[\\w/:%#$&?()~.=+\\-]+"

    /**
     * メッセージの日本語化をする
     * @param org メッセージ
     * @param type 日本語化のタイプ
     * @param dictionary 辞書
     * @return 日本語化したメッセージ
     */
    fun japanize(org: String, type: JapanizeType, dictionary: MutableMap<String, String>): String {
        // 変換不要なら空文字列を返す
        if (type == JapanizeType.NONE || !isNeedToJapanize(org)) {
            return ""
        }

        // URL削除
        val deletedURL = org.replace(REGEX_URL.toRegex(), " ")

        // キーワードをロック
        val keywordMap = mutableMapOf<String, String>()
        var index = 0
        var keywordLocked = deletedURL
        for (dickey in dictionary.keys) {
            if (keywordLocked.contains(dickey)) {
                index++
                val key = "＜" + makeMultibytesDigit(index) + "＞"
                keywordLocked = keywordLocked.replace(dickey, key)
                keywordMap[key] = dictionary[dickey]!!
            }
        }

        // カナ変換
        var japanized = YukiKanaConverter.conv(keywordLocked)

        // IME変換
        if (type == JapanizeType.GOOGLE_IME) {
            japanized = IMEConverter.convByGoogleIME(japanized)
        }

        // キーワードのアンロック
        for (key in keywordMap.keys) {
            japanized = japanized.replace(key, keywordMap[key]!!)
        }

        // 返す
        return japanized.trim()
    }

    /**
     * 日本語化が必要かどうかを判定する
     * @param org メッセージ
     * @return 日本語化が必要ならtrue
     */
    fun isNeedToJapanize(org: String): Boolean {
        return org.toByteArray().size == org.length && !org.matches("[ \\uFF61-\\uFF9F]+".toRegex())
    }

    /**
     * 数値を、全角文字の文字列に変換して返す
     * @param digit 文字
     * @return 全角文字
     */
    private fun makeMultibytesDigit(digit: Int): String {
        val half = digit.toString()
        val result = StringBuilder()
        for (index in half.indices) {
            when (half[index]) {
                '0' -> result.append("０")
                '1' -> result.append("１")
                '2' -> result.append("２")
                '3' -> result.append("３")
                '4' -> result.append("４")
                '5' -> result.append("５")
                '6' -> result.append("６")
                '7' -> result.append("７")
                '8' -> result.append("８")
                '9' -> result.append("９")
            }
        }
        return result.toString()
    }
}
