package net.somrpg.swordofmagic7.translater

/**
 * 日本語変換タイプ
 */
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
        @JvmStatic
        fun fromID(id: String?, def: JapanizeType): JapanizeType {
            if (id == null) return def
            for (type in entries) {
                if (type.id.equals(id, ignoreCase = true)) {
                    return type
                }
            }
            return def
        }
    }
}
