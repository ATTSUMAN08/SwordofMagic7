@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.builder

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import org.bukkit.GameMode
import swordofmagic7.Data.PlayerData

@CommandAlias("playmode|pm")
@CommandPermission("som7.builder")
class PlayModeCommand : BaseCommand() {
    @Default
    fun playMode(playerData: PlayerData) {
        playerData.playMode = !playerData.playMode
        if (playerData.playMode) {
            playerData.sendRichMessage("プレイモードを<green>ON</green>にしました")
            playerData.player.gameMode = GameMode.SURVIVAL
            playerData.player.closeInventory()
        } else {
            playerData.sendRichMessage("プレイモードを<red>OFF</red>にしました")
            playerData.player.gameMode = GameMode.CREATIVE
            playerData.player.inventory.clear()
        }
    }
}
