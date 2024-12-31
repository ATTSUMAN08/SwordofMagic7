package net.somrpg.swordofmagic7.translater

import com.google.gson.JsonArray
import com.google.gson.JsonParser

object GoogleIME {

    /**
     * GoogleIMEの最初の変換候補を抽出して結合します
     *
     * @param json 変換元のJson形式の文字列
     * @return 変換後の文字列
     * @since 2.8.10
     */
    fun parseJson(json: String): String {
        val result = StringBuilder()
        val jsonArray = JsonParser.parseString(json).asJsonArray
        for (response in jsonArray) {
            result.append(response.asJsonArray[1].asJsonArray[0].asString)
        }
        return result.toString()
    }
}
