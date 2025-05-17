package net.somrpg.swordofmagic7.utils

import org.bukkit.Bukkit

object ServerUtils {

    fun getColoredTPS(): String {
        val tps = Bukkit.getTPS()[0]
        val formattedTPS = String.format("%.1f", tps)
        val color = when {
            tps >= 18.0 -> "§a"
            tps >= 16.0 -> "§e"
            else -> "§c"
        }
        return "$color$formattedTPS"
    }

    fun getColoredMSPT(): String {
        val mspt = Bukkit.getAverageTickTime()
        val formattedMSPT = String.format("%.1f", mspt)
        val color = when {
            mspt <= 40.0 -> "§a"
            mspt <= 50.0 -> "§e"
            else -> "§c"
        }
        return "$color$formattedMSPT"
    }

    /**
     * サーバーのラグをパーセンテージで表示する関数
     * MSPTに基づいてラグの割合を計算し、適切な色でフォーマットして返す
     *
     * @return 色付きのラグパーセント文字列
     */
    fun getLagPercent(): String {
        val mspt = Bukkit.getAverageTickTime()

        return when {
            // 40mspt以下は最適なパフォーマンス (0%)
            mspt <= 40.0 -> "§a0%"

            // 40msptを超える場合、1msptごとに2%増加
            else -> {
                val percent = ((mspt - 40.0) * 2).toInt().coerceAtMost(100)
                val color = when {
                    percent <= 50 -> "§e"
                    else -> "§c"
                }
                "$color$percent%"
            }
        }
    }
}