@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.builder

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import org.bukkit.GameMode
import org.bukkit.entity.Player
import swordofmagic7.Data.PlayerData

@CommandAlias("playmode|pm")
@CommandPermission("som7.builder")
class PlayModeCommand : BaseCommand() {
    @Default
    fun playMode(sender: Player) {
        val playerData = PlayerData.playerData(sender)
        playerData.playMode = !playerData.playMode
        if (playerData.playMode) {
            sender.sendMessage("プレイモードをONにしました")
            sender.gameMode = GameMode.SURVIVAL
            sender.closeInventory()
        } else {
            sender.sendMessage("プレイモードをOFFにしました")
            sender.gameMode = GameMode.CREATIVE
            sender.inventory.clear()
        }
    }
}
