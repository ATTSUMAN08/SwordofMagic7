package net.somrpg.swordofmagic7.translater

import org.apache.commons.lang3.StringUtils

object YukiKanaConverter {

    private val MAP: Map<String, String>
    private val ROMAJI_LIST: Array<String>
    private val HIRAGANA_LIST: Array<String>

    init {
        val builder = mutableMapOf<String, String>()

        // ひらがな
        builder["a"] = "あ"
        builder["i"] = "い"
        builder["yi"] = "い"
        builder["u"] = "う"
        builder["wu"] = "う"
        builder["whu"] = "う"
        builder["e"] = "え"
        builder["o"] = "お"

        builder["wha"] = "うぁ"
        builder["whi"] = "うぃ"
        builder["wi"] = "うぃ"
        builder["whe"] = "うぇ"
        builder["we"] = "うぇ"
        builder["who"] = "うぉ"

        builder["wyi"] = "ゐ"
        builder["wye"] = "ゑ"

        builder["la"] = "ぁ"
        builder["xa"] = "ぁ"
        builder["li"] = "ぃ"
        builder["xi"] = "ぃ"
        builder["lyi"] = "ぃ"
        builder["xyi"] = "ぃ"
        builder["lu"] = "ぅ"
        builder["xu"] = "ぅ"
        builder["le"] = "ぇ"
        builder["xe"] = "ぇ"
        builder["lye"] = "ぇ"
        builder["xye"] = "ぇ"
        builder["lo"] = "ぉ"
        builder["xo"] = "ぉ"

        builder["ye"] = "いぇ"

        builder["ka"] = "か"
        builder["ca"] = "か"
        builder["ki"] = "き"
        builder["ku"] = "く"
        builder["cu"] = "く"
        builder["qu"] = "く"
        builder["ke"] = "け"
        builder["ko"] = "こ"
        builder["co"] = "こ"

        builder["kya"] = "きゃ"
        builder["kyi"] = "きぃ"
        builder["kyu"] = "きゅ"
        builder["kye"] = "きぇ"
        builder["kyo"] = "きょ"

        builder["qya"] = "くゃ"
        builder["qyu"] = "くゅ"
        builder["qyo"] = "くょ"

        builder["qwa"] = "くぁ"
        builder["qa"] = "くぁ"
        builder["kwa"] = "くぁ"
        builder["qwi"] = "くぃ"
        builder["qi"] = "くぃ"
        builder["qyi"] = "くぃ"
        builder["qwu"] = "くぅ"
        builder["qwe"] = "くぇ"
        builder["qe"] = "くぇ"
        builder["qye"] = "くぇ"
        builder["qwo"] = "くぉ"
        builder["qo"] = "くぉ"
        builder["kwo"] = "くぉ"

        builder["ga"] = "が"
        builder["gi"] = "ぎ"
        builder["gu"] = "ぐ"
        builder["ge"] = "げ"
        builder["go"] = "ご"

        builder["gya"] = "ぎゃ"
        builder["gyi"] = "ぎぃ"
        builder["gyu"] = "ぎゅ"
        builder["gye"] = "ぎぇ"
        builder["gyo"] = "ぎょ"

        builder["gwa"] = "ぐぁ"
        builder["gwi"] = "ぐぃ"
        builder["gwu"] = "ぐぅ"
        builder["gwe"] = "ぐぇ"
        builder["gwo"] = "ぐぉ"

        builder["lka"] = "ヵ"
        builder["xka"] = "ヵ"
        builder["lke"] = "ヶ"
        builder["xke"] = "ヶ"

        builder["sa"] = "さ"
        builder["si"] = "し"
        builder["ci"] = "し"
        builder["shi"] = "し"
        builder["su"] = "す"
        builder["se"] = "せ"
        builder["ce"] = "せ"
        builder["so"] = "そ"

        builder["sya"] = "しゃ"
        builder["sha"] = "しゃ"
        builder["syi"] = "しぃ"
        builder["syu"] = "しゅ"
        builder["shu"] = "しゅ"
        builder["sye"] = "しぇ"
        builder["she"] = "しぇ"
        builder["syo"] = "しょ"
        builder["sho"] = "しょ"

        builder["swa"] = "すぁ"
        builder["swi"] = "すぃ"
        builder["swu"] = "すぅ"
        builder["swe"] = "すぇ"
        builder["swo"] = "すぉ"

        builder["za"] = "ざ"
        builder["zi"] = "じ"
        builder["ji"] = "じ"
        builder["zu"] = "ず"
        builder["ze"] = "ぜ"
        builder["zo"] = "ぞ"

        builder["zya"] = "じゃ"
        builder["ja"] = "じゃ"
        builder["jya"] = "じゃ"
        builder["zyi"] = "じぃ"
        builder["jyi"] = "じぃ"
        builder["zyu"] = "じゅ"
        builder["ju"] = "じゅ"
        builder["jyu"] = "じゅ"
        builder["zye"] = "じぇ"
        builder["je"] = "じぇ"
        builder["jye"] = "じぇ"
        builder["zyo"] = "じょ"
        builder["jo"] = "じょ"
        builder["jyo"] = "じょ"

        builder["ta"] = "た"
        builder["ti"] = "ち"
        builder["chi"] = "ち"
        builder["tu"] = "つ"
        builder["tsu"] = "つ"
        builder["te"] = "て"
        builder["to"] = "と"

        builder["tya"] = "ちゃ"
        builder["cha"] = "ちゃ"
        builder["cya"] = "ちゃ"
        builder["tyi"] = "ちぃ"
        builder["cyi"] = "ちぃ"
        builder["tyu"] = "ちゅ"
        builder["chu"] = "ちゅ"
        builder["cyu"] = "ちゅ"
        builder["tye"] = "ちぇ"
        builder["che"] = "ちぇ"
        builder["cye"] = "ちぇ"
        builder["tyo"] = "ちょ"
        builder["cho"] = "ちょ"
        builder["cyo"] = "ちょ"

        builder["tsa"] = "つぁ"
        builder["tsi"] = "つぃ"
        builder["tse"] = "つぇ"
        builder["tso"] = "つぉ"

        builder["tha"] = "てゃ"
        builder["thi"] = "てぃ"
        builder["thu"] = "てゅ"
        builder["the"] = "てぇ"
        builder["tho"] = "てょ"

        builder["twa"] = "とぁ"
        builder["twi"] = "とぃ"
        builder["twu"] = "とぅ"
        builder["twe"] = "とぇ"
        builder["two"] = "とぉ"

        builder["da"] = "だ"
        builder["di"] = "ぢ"
        builder["du"] = "づ"
        builder["de"] = "で"
        builder["do"] = "ど"

        builder["dya"] = "ぢゃ"
        builder["dyi"] = "ぢぃ"
        builder["dyu"] = "ぢゅ"
        builder["dye"] = "ぢぇ"
        builder["dyo"] = "ぢょ"

        builder["dha"] = "でゃ"
        builder["dhi"] = "でぃ"
        builder["dhu"] = "でゅ"
        builder["dhe"] = "でぇ"
        builder["dho"] = "でょ"

        builder["dwa"] = "どぁ"
        builder["dwi"] = "どぃ"
        builder["dwu"] = "どぅ"
        builder["dwe"] = "どぇ"
        builder["dwo"] = "どぉ"

        builder["ltu"] = "っ"
        builder["xtu"] = "っ"
        builder["ltsu"] = "っ"
        builder["xtsu"] = "っ"

        builder["na"] = "な"
        builder["ni"] = "に"
        builder["nu"] = "ぬ"
        builder["ne"] = "ね"
        builder["no"] = "の"

        builder["nya"] = "にゃ"
        builder["nyi"] = "にぃ"
        builder["nyu"] = "にゅ"
        builder["nye"] = "にぇ"
        builder["nyo"] = "にょ"

        builder["ha"] = "は"
        builder["hi"] = "ひ"
        builder["hu"] = "ふ"
        builder["fu"] = "ふ"
        builder["he"] = "へ"
        builder["ho"] = "ほ"

        builder["hya"] = "ひゃ"
        builder["hyi"] = "ひぃ"
        builder["hyu"] = "ひゅ"
        builder["hye"] = "ひぇ"
        builder["hyo"] = "ひょ"

        builder["fwa"] = "ふぁ"
        builder["fa"] = "ふぁ"
        builder["fwi"] = "ふぃ"
        builder["fi"] = "ふぃ"
        builder["fyi"] = "ふぃ"
        builder["fwu"] = "ふぅ"
        builder["fwe"] = "ふぇ"
        builder["fe"] = "ふぇ"
        builder["fye"] = "ふぇ"
        builder["fwo"] = "ふぉ"
        builder["fo"] = "ふぉ"

        builder["fya"] = "ふゃ"
        builder["fyu"] = "ふゅ"
        builder["fyo"] = "ふょ"

        builder["ba"] = "ば"
        builder["bi"] = "び"
        builder["bu"] = "ぶ"
        builder["be"] = "べ"
        builder["bo"] = "ぼ"

        builder["bya"] = "びゃ"
        builder["byi"] = "びぃ"
        builder["byu"] = "びゅ"
        builder["bye"] = "びぇ"
        builder["byo"] = "びょ"

        builder["va"] = "ヴぁ"
        builder["vi"] = "ヴぃ"
        builder["vu"] = "ヴ"
        builder["ve"] = "ヴぇ"
        builder["vo"] = "ヴぉ"

        builder["vya"] = "ヴゃ"
        builder["vyi"] = "ヴぃ"
        builder["vyu"] = "ヴゅ"
        builder["vye"] = "ヴぇ"
        builder["vyo"] = "ヴょ"

        builder["pa"] = "ぱ"
        builder["pi"] = "ぴ"
        builder["pu"] = "ぷ"
        builder["pe"] = "ぺ"
        builder["po"] = "ぽ"

        builder["pya"] = "ぴゃ"
        builder["pyi"] = "ぴぃ"
        builder["pyu"] = "ぴゅ"
        builder["pye"] = "ぴぇ"
        builder["pyo"] = "ぴょ"

        builder["ma"] = "ま"
        builder["mi"] = "み"
        builder["mu"] = "む"
        builder["me"] = "め"
        builder["mo"] = "も"

        builder["mya"] = "みゃ"
        builder["myi"] = "みぃ"
        builder["myu"] = "みゅ"
        builder["mye"] = "みぇ"
        builder["myo"] = "みょ"

        builder["ya"] = "や"
        builder["yu"] = "ゆ"
        builder["yo"] = "よ"

        builder["lya"] = "ゃ"
        builder["xya"] = "ゃ"
        builder["lyu"] = "ゅ"
        builder["xyu"] = "ゅ"
        builder["lyo"] = "ょ"
        builder["xyo"] = "ょ"

        builder["ra"] = "ら"
        builder["ri"] = "り"
        builder["ru"] = "る"
        builder["re"] = "れ"
        builder["ro"] = "ろ"

        builder["rya"] = "りゃ"
        builder["ryi"] = "りぃ"
        builder["ryu"] = "りゅ"
        builder["rye"] = "りぇ"
        builder["ryo"] = "りょ"

        builder["wa"] = "わ"
        builder["wo"] = "を"

        builder["lwa"] = "ゎ"
        builder["xwa"] = "ゎ"

        builder["n"] = "ん"
        builder["nn"] = "ん"
        builder["n'"] = "ん"
        builder["xn"] = "ん"

        // 促音を追加する
        for ((romaji, hiragana) in builder) {
            if (canStartFromSokuon(romaji)) {
                builder[romaji[0] + romaji] = "っ$hiragana"
            }
        }

        // 記号とか
        builder["-"] = "ー"
        builder[","] = "、"
        builder["."] = "。"
        builder["?"] = "？"
        builder["!"] = "！"
        builder["["] = "「"
        builder["]"] = "」"
        builder["<"] = "＜"
        builder[">"] = "＞"
        builder["&"] = "＆"
        builder["\""] = "”"
        builder["("] = "（"
        builder[")"] = "）"

        MAP = builder.toMap()

        ROMAJI_LIST = MAP.keys.toTypedArray()
        HIRAGANA_LIST = MAP.values.toTypedArray()
    }

    /**
     * 「っ」や「ッ」などの促音から開始できるかどうか
     *
     * @param romaji 検証したい「ローマ字」
     * @return 促音から開始できるかどうか
     * @since 2.8.10
     */
    private fun canStartFromSokuon(romaji: String): Boolean {
        return !StringUtils.startsWithAny(romaji, "a", "i", "u", "e", "o", "n")
    }

    /**
     * 「ローマ字」から「かな文字」に変換する
     *
     * @param romaji 変換元の「ローマ字」
     * @return 変換後の「かな文字」
     * @since 2.8.10
     */
    fun conv(romaji: String): String {
        return StringUtils.replaceEach(romaji, ROMAJI_LIST, HIRAGANA_LIST)
    }
}
