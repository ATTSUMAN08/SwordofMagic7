package net.somrpg.swordofmagic7.placeholders

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.somrpg.swordofmagic7.extensions.getPlayerData
import net.somrpg.swordofmagic7.utils.ServerUtils
import org.bukkit.OfflinePlayer

class SomPlaceholder : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "som7"
    }

    override fun getAuthor(): String {
        return "ATTSUMAN08"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun onRequest(player: OfflinePlayer, params: String): String {
        return when (params) {
            "tps" -> ServerUtils.getColoredTPS()
            "mspt" -> ServerUtils.getColoredMSPT()
            "lag" -> ServerUtils.getLagPercent()
            "displayname" -> {
                val p = player.player
                if (p == null || !p.isOnline) ""
                else p.getPlayerData().displayName
            }
            else -> ""
        }
    }

    override fun persist(): Boolean {
        return true
    }
}