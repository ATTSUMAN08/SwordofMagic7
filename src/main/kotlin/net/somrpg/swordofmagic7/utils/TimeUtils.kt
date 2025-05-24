package net.somrpg.swordofmagic7.utils

object TimeUtils {

    /**
     * 秒を時間形式の文字列に変換する
     * @param seconds 秒数
     * @return 時間形式の文字列（例: "1時間30分45秒", "30分", "59秒"）
     */
    fun formatSeconds(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return when {
            hours > 0 && minutes > 0 && remainingSeconds > 0 -> "${hours}時間${minutes}分${remainingSeconds}秒"
            hours > 0 && minutes > 0 -> "${hours}時間${minutes}分"
            hours > 0 && remainingSeconds > 0 -> "${hours}時間${remainingSeconds}秒"
            hours > 0 -> "${hours}時間"
            minutes > 0 && remainingSeconds > 0 -> "${minutes}分${remainingSeconds}秒"
            minutes > 0 -> "${minutes}分"
            else -> "${remainingSeconds}秒"
        }
    }

    /**
     * ミリ秒を時間形式の文字列に変換する
     * @param milliseconds ミリ秒数
     * @return 時間形式の文字列
     */
    fun formatMilliseconds(milliseconds: Long): String {
        return formatSeconds((milliseconds / 1000).toInt())
    }
}