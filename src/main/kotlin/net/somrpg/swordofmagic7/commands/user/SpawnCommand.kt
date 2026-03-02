@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.user

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
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
