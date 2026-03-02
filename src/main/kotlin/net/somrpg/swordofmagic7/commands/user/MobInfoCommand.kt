@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.user

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Syntax
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player
import swordofmagic7.Data.DataBase
import swordofmagic7.Function
import swordofmagic7.Mob.EnemyData
import swordofmagic7.Sound.SoundList

@CommandAlias("mobInfo|mi")
@CommandPermission("som7.user")
class MobInfoCommand : BaseCommand() {
    @Default
    @Syntax("[id] [level]")
    @CommandCompletion("@visibleMobs *")
    fun default(
        player: Player,
        @Optional id: String?,
        @Optional levelStr: String?,
    ) {
        val playerData = player.getPlayerData()
        if (id == null) {
            playerData.Menu.mobInfo.MobInfoView()
            return
        }
        if (!DataBase.MobList.containsKey(id)) {
            Function.sendMessage(player, "§a存在しない§cエネミー§aです", SoundList.NOPE)
            return
        }
        val mobData = DataBase.getMobData(id)
        val message = mutableListOf<String>()
        message.add(Function.decoText(mobData.Display))
        message.addAll(playerData.Menu.mobInfo.toStringList(mobData))
        levelStr?.toIntOrNull()?.let { message.addAll(EnemyData.enemyLore(mobData, it)) }
        Function.sendMessage(player, message, SoundList.NOPE)
    }
}
