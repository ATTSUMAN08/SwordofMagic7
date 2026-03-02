@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.developer

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player
import swordofmagic7.MultiThread.MultiThread

@CommandAlias("skillCTReset")
@CommandPermission("som7.developer")
class SkillCTResetCommand : BaseCommand() {
    @Default
    fun default(player: Player) {
        val playerData = player.getPlayerData()
        for (skillData in playerData.Skill.SkillCoolTime.keys) {
            MultiThread.TaskRunSynchronizedLater(
                { playerData.Skill.resetSkillCoolTime(skillData) },
                1,
            )
        }
    }
}
