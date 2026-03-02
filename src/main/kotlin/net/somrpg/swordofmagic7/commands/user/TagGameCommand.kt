@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.user

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player
import swordofmagic7.Function
import swordofmagic7.TagGame

@CommandAlias("taggame|tagGame")
@CommandPermission("som7.user")
class TagGameCommand : BaseCommand() {
    @Default
    fun info(player: Player) {
        for (str in TagGame.info()) player.sendMessage(str)
        player.sendMessage("§e/tagGame [join/leave]")
    }

    @Subcommand("join")
    fun join(player: Player) {
        TagGame.join(player)
    }

    @Subcommand("leave")
    fun leave(player: Player) {
        TagGame.leave(player)
    }

    @Subcommand("start")
    fun start(player: Player) {
        if (TagGame.Master == player) TagGame.startTagGame()
    }

    @Subcommand("reset")
    fun reset(player: Player) {
        if (TagGame.Master == player) TagGame.resetTagGame()
    }

    @Subcommand("master")
    fun master(player: Player) {
        if (TagGame.Master == null || !TagGame.Master.isOnline()) {
            TagGame.Master = player
            Function.sendMessage(player, "§eゲームマスター§aになりました")
        } else {
            Function.sendMessage(player, "${TagGame.Master.displayName}§aが§eゲームマスター§aです")
        }
    }
}
