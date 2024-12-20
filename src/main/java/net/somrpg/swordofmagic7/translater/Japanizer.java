package net.somrpg.swordofmagic7.translater;

import java.util.HashMap;
import java.util.Map;

/**
 * ローマ字表記を漢字変換して返すユーティリティ
 * @author ucchy
 */
public class Japanizer {

    public Japanizer() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final String REGEX_URL = "https?://[\\w/:%#$&?()~.=+\\-]+";

    /**
     * メッセージの日本語化をする
     * @param org メッセージ
     * @param type 日本語化のタイプ
     * @param dictionary 辞書
     * @return 日本語化したメッセージ
     */
    public static String japanize(String org, JapanizeType type,
                                  Map<String, String> dictionary) {

        // 変換不要なら空文字列を返す
        if ( type == JapanizeType.NONE || !isNeedToJapanize(org) ) {
            return "";
        }

        // URL削除
        String deletedURL = org.replaceAll(REGEX_URL, " ");

        // キーワードをロック
        HashMap<String, String> keywordMap = new HashMap<>();
        int index = 0;
        String keywordLocked = deletedURL;
        for ( String dickey : dictionary.keySet() ) {
            if ( keywordLocked.contains(dickey) ) {
                index++;
                String key = "＜" + makeMultibytesDigit(index) + "＞";
                keywordLocked = keywordLocked.replace(dickey, key);
                keywordMap.put(key, dictionary.get(dickey));
            }
        }

        // カナ変換
        String japanized = YukiKanaConverter.conv(keywordLocked);

        // IME変換
        if ( type == JapanizeType.GOOGLE_IME ) {
            japanized = IMEConverter.convByGoogleIME(japanized);
        }

        // キーワードのアンロック
        for ( String key : keywordMap.keySet() ) {
            japanized = japanized.replace(key, keywordMap.get(key));
        }

        // 返す
        return japanized.trim();
    }

    /**
     * 日本語化が必要かどうかを判定する
     * @param org メッセージ
     * @return 日本語化が必要ならtrue
     */
   public static boolean isNeedToJapanize(String org) {
        return ( org.getBytes().length == org.length()
                && !org.matches("[ \\uFF61-\\uFF9F]+") );
    }

    /**
     * 数値を、全角文字の文字列に変換して返す
     * @param digit 文字
     * @return 全角文字
     */
    private static String makeMultibytesDigit(int digit) {

        String half = Integer.toString(digit);
        StringBuilder result = new StringBuilder();
        for ( int index=0; index < half.length(); index++ ) {
            switch ( half.charAt(index) ) {
                case '0' : result.append("０"); break;
                case '1' : result.append("１"); break;
                case '2' : result.append("２"); break;
                case '3' : result.append("３"); break;
                case '4' : result.append("４"); break;
                case '5' : result.append("５"); break;
                case '6' : result.append("６"); break;
                case '7' : result.append("７"); break;
                case '8' : result.append("８"); break;
                case '9' : result.append("９"); break;
            }
        }
        return result.toString();
    }
}