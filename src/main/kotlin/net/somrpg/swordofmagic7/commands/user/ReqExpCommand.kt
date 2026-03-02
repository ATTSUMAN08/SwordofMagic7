@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.user

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import org.bukkit.command.CommandSender
import swordofmagic7.classes.Classes

@CommandAlias("reqExp")
@CommandPermission("som7.user")
class ReqExpCommand : BaseCommand() {
    @Default
    @Syntax("<level>")
    fun default(
        sender: CommandSender,
        level: Int,
    ) {
        val reqExp = Classes.reqExp(level)
        sender.sendMessage("§eLv$level§7: §a$reqExp")
    }
}
