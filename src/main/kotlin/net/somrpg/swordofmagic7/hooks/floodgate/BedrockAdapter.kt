package net.somrpg.swordofmagic7.hooks.floodgate

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.geysermc.floodgate.api.FloodgateApi

object BedrockAdapter {

    fun isBedrock(player: Player): Boolean {
        if (Bukkit.getPluginManager().isPluginEnabled("Floodgate")) {
            val api = FloodgateApi.getInstance()
            return api.isFloodgateId(player.uniqueId)
        }
        return false
    }
}