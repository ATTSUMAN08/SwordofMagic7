/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2020
 *
 * Porting from Java to Kotlin
 */
package net.somrpg.swordofmagic7.translater

import com.google.common.io.CharStreams
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * ひらがなのみの文章を、IMEを使用して変換します。
 * 使用される変換候補は全て第1候補のため、正しくない結果が含まれることもよくあります。
 */
object IMEConverter {
    private const val GOOGLE_IME_URL =
        "https://www.google.com/transliterate?langpair=ja-Hira|ja&text="

    /**
     * GoogleIMEを使って変換する
     * @param org 変換元
     * @return 変換後
     */
    fun convByGoogleIME(org: String): String {
        if (org.isEmpty()) {
            return ""
        }

        var urlconn: HttpURLConnection? = null
        var reader: BufferedReader? = null
        return try {
            val baseurl = GOOGLE_IME_URL + URLEncoder.encode(org, "UTF-8")
            val encode = "UTF-8"

            @Suppress("DEPRECATION")
            val url = URL(baseurl)

            urlconn = url.openConnection() as HttpURLConnection
            urlconn.connectTimeout = 1000
            urlconn.readTimeout = 1000
            urlconn.requestMethod = "GET"
            urlconn.instanceFollowRedirects = false
            urlconn.connect()

            val responseCode = urlconn.responseCode

            val inputStream =
                if (responseCode in 200..299) {
                    urlconn.inputStream
                } else {
                    urlconn.errorStream
                }
            if (inputStream == null) {
                return org
            }

            reader = BufferedReader(InputStreamReader(inputStream, encode))

            val json = CharStreams.toString(reader)
            val parsed = GoogleIME.parseJson(json)
            parsed
        } catch (_: Exception) {
            org
        } finally {
            urlconn?.disconnect()
            try {
                reader?.close()
            } catch (_: Exception) {
                // ignore
            }
        }
    }
}
