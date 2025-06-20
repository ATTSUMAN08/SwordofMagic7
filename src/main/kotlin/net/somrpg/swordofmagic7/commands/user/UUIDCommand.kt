@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.user

import me.attsuman08.abysslib.shade.acf.BaseCommand
import me.attsuman08.abysslib.shade.acf.annotation.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("uuid")
@CommandPermission("som7.user")
class UUIDCommand : BaseCommand() {

    @Default
    @Syntax("<player>")
    @CommandCompletion("@players")
    fun default(sender: CommandSender, @Optional playerId: String? = null) {
        if (playerId == null && sender !is Player) {
            sender.sendMessage("§cコンソールから実行する場合はMCIDを指定してください")
            return
        }
        val targetPlayer = Bukkit.getOfflinePlayerIfCached(playerId!!) ?: run {
            sender.sendMessage("§c指定されたプレイヤーはログインしたことがありません")
            return
        }

        sender.sendMessage(Component.text("${targetPlayer.name}のUUID: ${targetPlayer.uniqueId}")
            .clickEvent(ClickEvent.copyToClipboard(targetPlayer.uniqueId.toString()))
            .hoverEvent(Component.text("§eクリックしてUUIDをコピー"))
        )
    }
}