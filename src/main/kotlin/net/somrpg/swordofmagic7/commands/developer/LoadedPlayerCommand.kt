@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.developer

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import org.bukkit.command.CommandSender
import swordofmagic7.Data.PlayerData

@CommandAlias("loadedPlayer")
@CommandPermission("som7.developer")
class LoadedPlayerCommand : BaseCommand() {
    @Default
    fun default(sender: CommandSender) {
        sender.sendMessage("Loaded PlayerData: ")
        for (playerData in PlayerData.getPlayerData().values) {
            sender.sendMessage("${playerData.player.uniqueId}: ${playerData.player.name}")
        }
    }
}
