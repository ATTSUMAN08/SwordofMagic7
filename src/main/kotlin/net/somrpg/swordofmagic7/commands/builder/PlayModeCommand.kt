@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.builder

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import kotlinx.coroutines.withContext
import net.somrpg.swordofmagic7.SomCore
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import swordofmagic7.Data.PlayerData

class PlayModeCommand {

    @Command("playmode|pm")
    @Permission("som7.builder")
    suspend fun playMode(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage("このコマンドはプレイヤー専用です")
            return
        }
        val playerData = PlayerData.playerData(sender)
        playerData.playMode = !playerData.playMode
        withContext(SomCore.instance.minecraftDispatcher) {
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
}