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

@CommandAlias("setNick")
@CommandPermission("som7.developer")
class SetNickCommand : BaseCommand() {
    @Default
    @Syntax("<nick> [player]")
    @CommandCompletion("* @players")
    fun default(
        sender: CommandSender,
        nick: String,
        @Optional target: Player?,
    ) {
        val t =
            target ?: (sender as? Player) ?: run {
                sender.sendMessage("§c無効なプレイヤーです")
                return
            }
        val targetData = PlayerData.playerData(t)
        targetData.Nick = nick
        sender.sendMessage(targetData.Nick)
        targetData.Status.StatusUpdate()
    }
}
