@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.user.settings

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player

@CommandAlias("holoselfview|hsv")
@CommandPermission("som7.user")
class HoloSelfViewCommand : BaseCommand() {
    @Default
    fun default(sender: Player) {
        sender.getPlayerData().HoloSelfView()
    }
}
