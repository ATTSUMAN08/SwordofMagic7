package net.somrpg.swordofmagic7.translater

enum class JapanizeType(val id: String) {

    /** 日本語変換をしない */
    NONE("none"),

    /** カナ変換のみする */
    KANA("kana"),

    /** カナ変換後、GoogleIMEで漢字変換 */
    GOOGLE_IME("googleime");

    override fun toString(): String {
        return id
    }

    companion object {
        /**
         * 文字列表記からJapanizeTypeを作成して返す
         * @param id ID
         * @param def デフォルト
         * @return JapanizeType
         */
        fun fromID(id: String?, def: JapanizeType): JapanizeType {
            if (id == null) return def
            for (type in values()) {
                if (type.id.equals(id, ignoreCase = true)) {
                    return type
                }
            }
            return def
        }
    }
}
