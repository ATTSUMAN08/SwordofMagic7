@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.developer

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import org.bukkit.command.CommandSender
import swordofmagic7.Client

@CommandAlias("sendData")
@CommandPermission("som7.developer")
class SendDataCommand : BaseCommand() {
    @Default
    @Syntax("<text>")
    fun default(
        sender: CommandSender,
        text: String,
    ) {
        Client.send(text)
    }
}
