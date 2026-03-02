@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.developer

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Syntax
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import swordofmagic7.Data.PlayerData

@CommandAlias("addTitle")
@CommandPermission("som7.developer")
class AddTitleCommand : BaseCommand() {
    @Default
    @Syntax("<title> [player]")
    @CommandCompletion("@titles @players")
    fun default(
        sender: CommandSender,
        titleId: String,
        @Optional target: Player?,
    ) {
        val t =
            target ?: (sender as? Player) ?: run {
                sender.sendMessage("§c無効なプレイヤーです")
                return
            }
        PlayerData.playerData(t).titleManager.addTitle(titleId)
    }
}
