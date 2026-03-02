@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.user

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Syntax
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player
import swordofmagic7.Function

@CommandAlias("playerInfo|info|i")
class PlayerInfoCommand : BaseCommand() {
    @Default
    @Syntax("[player]")
    @CommandCompletion("@players")
    fun default(
        player: Player,
        @Optional target: Player?,
    ) {
        val t = target ?: player
        if (Function.CheckBlockPlayer(player, t)) return
        player
            .getPlayerData()
            .Menu.StatusInfo
            .StatusInfoView(t)
    }
}
