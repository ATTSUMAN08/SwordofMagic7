@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.builder

import me.attsuman08.abysslib.shade.acf.BaseCommand
import me.attsuman08.abysslib.shade.acf.annotation.CommandAlias
import me.attsuman08.abysslib.shade.acf.annotation.CommandPermission
import me.attsuman08.abysslib.shade.acf.annotation.Default
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