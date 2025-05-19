@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.user

import me.attsuman08.abysslib.shade.acf.BaseCommand
import me.attsuman08.abysslib.shade.acf.annotation.CommandAlias
import me.attsuman08.abysslib.shade.acf.annotation.CommandPermission
import me.attsuman08.abysslib.shade.acf.annotation.Default
import net.somrpg.swordofmagic7.SomCore
import org.bukkit.entity.Player
import swordofmagic7.Data.PlayerData
import swordofmagic7.TagGame

@CommandAlias("spawn")
@CommandPermission("som7.user")
class SpawnCommand : BaseCommand() {

    @Default
    fun default(sender: Player) {
        if (TagGame.isTagPlayerNonMessage(sender)) return
        if (PlayerData.playerData(sender).isPvPModeNonMessage()) return
        SomCore.instance.spawnPlayer(sender)
    }
}