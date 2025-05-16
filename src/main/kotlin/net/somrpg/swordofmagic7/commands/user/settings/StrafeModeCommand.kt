@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.user.settings

import me.attsuman08.abysslib.shade.acf.BaseCommand
import me.attsuman08.abysslib.shade.acf.annotation.CommandAlias
import me.attsuman08.abysslib.shade.acf.annotation.CommandPermission
import me.attsuman08.abysslib.shade.acf.annotation.Default
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player

@CommandAlias("strafemode")
@CommandPermission("som7.user")
class StrafeModeCommand : BaseCommand() {

    @Default
    fun default(sender: Player) {
        sender.getPlayerData().StrafeMode()
    }
}