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

@CommandAlias("attribute|attr|a")
@CommandPermission("som7.user")
class AttributeCommand : BaseCommand() {

    @Default
    fun default(sender: Player) {
        sender.getPlayerData().Attribute.AttributeMenuView()
        CustomSound.playSound(sender, SoundList.MENU_OPEN)
    }
}