package net.somrpg.swordofmagic7.translater

import com.google.gson.Gson
import com.google.gson.JsonArray

object GoogleIME {
    /**
     * GoogleIMEの最初の変換候補を抽出して結合します
     *
     * @param json 変換元のJson形式の文字列
     * @return 変換後の文字列
     * @since 2.8.10
     */
    fun parseJson(json: String): String =
        try {
            val result = StringBuilder()
            val array = Gson().fromJson(json, JsonArray::class.java)

            for ((index, response) in array.withIndex()) {
                val segment = response.asJsonArray[1].asJsonArray[0].asString
                result.append(segment)
            }

            val parsed = result.toString()
            parsed
        } catch (e: Exception) {
            throw IllegalArgumentException("GoogleIMEの変換に失敗しました。", e)
        }
}
