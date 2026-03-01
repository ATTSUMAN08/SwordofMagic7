@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.user

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player

@CommandAlias("enemykills")
@CommandPermission("som7.user")
class EnemyKillsCommand : BaseCommand() {

    @Default
    @Syntax("<target>")
    fun default(sender: Player, @Optional target: Player?) {
        val player = target ?: sender
        val data = player.getPlayerData()

        val kills = data.statistics.EnemyKills.toList()
            .sortedByDescending { it.second }
            .take(20)

        sender.sendMessage("§a${player.name}のエネミーキル数TOP20")
        if (kills.isEmpty()) {
            sender.sendMessage("§cなし")
            return
        }
        kills.forEach { (enemy, count) ->
            sender.sendMessage("§e$enemy: §a$count")
        }
    }
}