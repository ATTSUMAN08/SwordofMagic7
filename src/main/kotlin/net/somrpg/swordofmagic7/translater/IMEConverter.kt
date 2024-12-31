package net.somrpg.swordofmagic7.translater

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * ひらがなのみの文章を、IMEを使用して変換します。
 * 使用される変換候補は全て第1候補のため、正しくない結果が含まれることもよくあります。
 */
object IMEConverter {
    private const val GOOGLE_IME_URL = "https://www.google.com/transliterate?langpair=ja-Hira|ja&text="

    /**
     * GoogleIMEを使って変換する
     * @param org 変換元
     * @return 変換後
     */
    fun convByGoogleIME(org: String): String {
        if (org.isEmpty()) {
            return ""
        }

        val client = HttpClient.newBuilder().build()
        val encodedText = URLEncoder.encode(org, StandardCharsets.UTF_8)
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$GOOGLE_IME_URL$encodedText"))
            .GET()
            .build()

        return try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            GoogleIME.parseJson(response.body())
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
