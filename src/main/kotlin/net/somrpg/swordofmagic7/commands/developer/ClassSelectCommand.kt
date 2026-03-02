@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.developer

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player
import swordofmagic7.Data.DataBase
import swordofmagic7.classes.Classes

@CommandAlias("classSelect")
@CommandPermission("som7.developer")
class ClassSelectCommand : BaseCommand() {
    @Default
    @Syntax("<slot> <class>")
    @CommandCompletion("@classSlots @classes")
    fun default(
        player: Player,
        @Conditions("limits:min=1,max=${Classes.maxSlot}") slot: Int,
        className: String,
    ) {
        player.getPlayerData().Classes.classSlot[slot - 1] = DataBase.getClassData(className)
    }
}
