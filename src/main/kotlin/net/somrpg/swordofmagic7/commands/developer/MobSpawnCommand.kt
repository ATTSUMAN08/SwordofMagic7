@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.developer

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import org.bukkit.entity.Player
import swordofmagic7.Data.DataBase
import swordofmagic7.Mob.MobManager

@CommandAlias("mobSpawn|ms")
@CommandPermission("som7.developer")
class MobSpawnCommand : BaseCommand() {
    @Default
    @Syntax("<id> [level] [amount]")
    fun default(
        player: Player,
        id: String,
        @Default("1") level: Int,
        @Default("1") amount: Int,
    ) {
        if (!DataBase.getMobList().containsKey(id)) {
            DataBase.getMobList().keys.forEach { player.sendMessage(it) }
            return
        }
        repeat(amount) {
            MobManager.mobSpawn(DataBase.getMobData(id), level, player.location)
        }
    }
}
