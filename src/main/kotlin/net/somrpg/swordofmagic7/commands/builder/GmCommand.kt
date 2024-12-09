@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.builder

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import kotlinx.coroutines.withContext
import net.somrpg.swordofmagic7.SomCore
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission

class GmCommand {

    @Command("gm [mode]")
    @Permission("som7.builder")
    suspend fun gm(sender: CommandSender, @Argument("mode") mode: String?) {
        if (sender !is Player) {
            sender.sendMessage("このコマンドはプレイヤー専用です")
            return
        }

        withContext(SomCore.instance.minecraftDispatcher) {
            when (mode) {
                "0", "s", "survival" -> sender.gameMode = GameMode.SURVIVAL
                "1", "c", "creative" -> sender.gameMode = GameMode.CREATIVE
                "2", "a", "adventure" -> sender.gameMode = GameMode.ADVENTURE
                "3", "sp", "spectator" -> sender.gameMode = GameMode.SPECTATOR
                else -> {
                    if (sender.gameMode == GameMode.CREATIVE) {
                        sender.gameMode = GameMode.SURVIVAL
                    } else {
                        sender.gameMode = GameMode.CREATIVE
                    }
                }
            }
        }
    }
}