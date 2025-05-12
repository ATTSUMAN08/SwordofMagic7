package net.somrpg.swordofmagic7.extensions

import org.bukkit.entity.Player
import swordofmagic7.Data.PlayerData

fun Player.getPlayerData(): PlayerData {
    return PlayerData.playerData(this)
}