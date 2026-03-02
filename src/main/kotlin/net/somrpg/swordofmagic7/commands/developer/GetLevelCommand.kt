@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.developer

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player

@CommandAlias("getLevel")
@CommandPermission("som7.developer")
class GetLevelCommand : BaseCommand() {
    @Default
    @Syntax("<amount>")
    fun default(
        player: Player,
        amount: Int,
    ) {
        player.getPlayerData().addPlayerLevel(amount)
    }
}
