@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.builder

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Syntax
import org.bukkit.GameMode
import org.bukkit.entity.Player

@CommandAlias("gm")
@CommandPermission("som7.builder")
class GmCommand : BaseCommand() {
    @Default
    @Syntax("<mode>")
    fun gm(
        player: Player,
        @Optional mode: String?,
    ) {
        when (mode) {
            "0", "s", "survival" -> {
                player.gameMode = GameMode.SURVIVAL
            }

            "1", "c", "creative" -> {
                player.gameMode = GameMode.CREATIVE
            }

            "2", "a", "adventure" -> {
                player.gameMode = GameMode.ADVENTURE
            }

            "3", "sp", "spectator" -> {
                player.gameMode = GameMode.SPECTATOR
            }

            else -> {
                if (player.gameMode == GameMode.CREATIVE) {
                    player.gameMode = GameMode.SURVIVAL
                } else {
                    player.gameMode = GameMode.CREATIVE
                }
            }
        }
    }
}
