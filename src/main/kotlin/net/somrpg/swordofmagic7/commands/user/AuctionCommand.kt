@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.user

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CatchUnknown
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Syntax
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player
import swordofmagic7.Auction

@CommandAlias("auction|auc")
@CommandPermission("som7.user")
class AuctionCommand : BaseCommand() {
    @Default
    @CatchUnknown
    @Syntax("[subCommand] [arg]")
    fun default(
        player: Player,
        @Optional subCommand: String?,
        @Optional arg: String?,
    ) {
        val args = listOfNotNull(subCommand, arg).toTypedArray()
        Auction.auctionCommand(player.getPlayerData(), args)
    }
}
