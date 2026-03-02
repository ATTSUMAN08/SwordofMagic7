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

@CommandAlias("getClassExp")
@CommandPermission("som7.developer")
class GetClassExpCommand : BaseCommand() {
    @Default
    @Syntax("<amount> <class>")
    @CommandCompletion("* @classes")
    fun default(
        player: Player,
        amount: Int,
        className: String,
    ) {
        if (!DataBase.getClassList().containsKey(className)) {
            player.sendMessage("§c/getClassExp <exp> <class>")
            return
        }
        player.getPlayerData().Classes.addClassExp(DataBase.getClassData(className), amount)
    }
}
