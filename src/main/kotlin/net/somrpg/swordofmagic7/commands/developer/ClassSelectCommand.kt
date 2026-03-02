@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.developer

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player
import swordofmagic7.Data.DataBase

@CommandAlias("classSelect")
@CommandPermission("som7.developer")
class ClassSelectCommand : BaseCommand() {
    @Default
    @Syntax("<slot> <class>")
    @CommandCompletion("* @classes")
    fun default(
        player: Player,
        slot: Int,
        className: String,
    ) {
        try {
            player.getPlayerData().Classes.classSlot[slot] = DataBase.getClassData(className)
        } catch (e: Exception) {
            player.sendMessage("/classSelect <slot> <class>")
        }
    }
}
