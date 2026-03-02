@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.user

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CatchUnknown
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Syntax
import org.bukkit.entity.Player
import swordofmagic7.Data.PlayerData
import swordofmagic7.Party.PartyManager

@CommandAlias("party|pt")
@CommandPermission("som7.user")
class PartyCommand : BaseCommand() {
    @Default
    @CatchUnknown
    @Syntax("[subCommand] [player]")
    @CommandCompletion("* @players")
    fun default(
        player: Player,
        @Optional subCommand: String?,
        @Optional arg: String?,
    ) {
        val args = listOfNotNull(subCommand, arg).toTypedArray()
        PartyManager.partyCommand(player, PlayerData.playerData(player), args)
    }
}
