@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.user

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player
import swordofmagic7.Sound.CustomSound
import swordofmagic7.Sound.SoundList

@CommandAlias("skill|s")
@CommandPermission("som7.user")
class SkillCommand : BaseCommand() {

    @Default
    fun default(sender: Player) {
        sender.getPlayerData().Skill.SkillMenuView()
        CustomSound.playSound(sender, SoundList.MENU_OPEN)
    }
}