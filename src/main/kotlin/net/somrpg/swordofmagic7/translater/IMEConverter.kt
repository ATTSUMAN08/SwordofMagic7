package net.somrpg.swordofmagic7.translater

import com.google.common.io.CharStreams
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * ひらがなのみの文章を、IMEを使用して変換します。
 * 使用される変換候補は全て第1候補のため、正しくない結果が含まれることもよくあります。
 */
class IMEConverter {
    companion object {
        private const val GOOGLE_IME_URL = "https://www.google.com/transliterate?langpair=ja-Hira|ja&text="

        /**
         * GoogleIMEを使って変換する
         * @param org 変換元
         * @return 変換後
         */
        @JvmStatic
        fun convByGoogleIME(org: String): String {
            if (org.isEmpty()) {
                return ""
            }

            var urlconn: HttpURLConnection? = null
            var reader: BufferedReader? = null
            return try {
                val baseurl = GOOGLE_IME_URL + URLEncoder.encode(org, StandardCharsets.UTF_8)
                val encode = "UTF-8"
                val url = URI.create(baseurl).toURL()

                urlconn = url.openConnection() as HttpURLConnection
                urlconn.requestMethod = "GET"
                urlconn.instanceFollowRedirects = false
                urlconn.connect()

                reader = BufferedReader(InputStreamReader(urlconn.inputStream, encode))
                val json = CharStreams.toString(reader)

                GoogleIME.parseJson(json)
            } catch (e: IOException) {
                e.printStackTrace()
                ""
            } finally {
                urlconn?.disconnect()
                reader?.close()
            }
        }
    }
}
