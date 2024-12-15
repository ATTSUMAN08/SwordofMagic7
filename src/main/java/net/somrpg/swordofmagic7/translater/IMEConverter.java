package net.somrpg.swordofmagic7.translater;

import com.google.common.io.CharStreams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * ひらがなのみの文章を、IMEを使用して変換します。
 * 使用される変換候補は全て第1候補のため、正しくない結果が含まれることもよくあります。
 * @author ucchy
 */
public class IMEConverter {
    private static final String GOOGLE_IME_URL =
            "https://www.google.com/transliterate?langpair=ja-Hira|ja&text=";

    /**
     * GoogleIMEを使って変換する
     * @param org 変換元
     * @return 変換後
     */
    public static String convByGoogleIME(String org) {

        if (org.isEmpty()) {
            return "";
        }

        HttpURLConnection urlconn = null;
        BufferedReader reader = null;
        try {
            String baseurl;
            String encode;
            baseurl = GOOGLE_IME_URL + URLEncoder.encode(org , StandardCharsets.UTF_8);
            encode = "UTF-8";
            URL url = new URL(baseurl);

            urlconn = (HttpURLConnection)url.openConnection();
            urlconn.setRequestMethod("GET");
            urlconn.setInstanceFollowRedirects(false);
            urlconn.connect();

            reader = new BufferedReader(
                    new InputStreamReader(urlconn.getInputStream(), encode));

            String json = CharStreams.toString(reader);

            return GoogleIME.parseJson(json);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( urlconn != null ) {
                urlconn.disconnect();
            }
            if ( reader != null ) {
                try {
                    reader.close();
                } catch (IOException e) { // do nothing.
                }
            }
        }

        return "";
    }

}