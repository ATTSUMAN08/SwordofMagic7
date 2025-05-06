@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.builder

import me.attsuman08.abysslib.shade.acf.BaseCommand
import me.attsuman08.abysslib.shade.acf.annotation.*
import org.bukkit.GameMode
import org.bukkit.entity.Player

@CommandAlias("gm")
@CommandPermission("som7.builder")
class GmCommand : BaseCommand() {

    @Default
    @Syntax("<mode>")
    fun gm(sender: Player, @Optional mode: String?) {
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