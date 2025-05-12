@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.user

import me.attsuman08.abysslib.shade.acf.BaseCommand
import me.attsuman08.abysslib.shade.acf.annotation.CommandAlias
import me.attsuman08.abysslib.shade.acf.annotation.CommandPermission
import me.attsuman08.abysslib.shade.acf.annotation.Default
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