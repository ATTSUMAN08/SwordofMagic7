@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.developer

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player
import swordofmagic7.Effect.EffectType

@CommandAlias("getEffect")
@CommandPermission("som7.developer")
class GetEffectCommand : BaseCommand() {
    @Default
    @Syntax("<effect> [time]")
    @CommandCompletion("@effects *")
    fun default(
        player: Player,
        effectName: String,
        @Default("200") time: Int,
    ) {
        try {
            player.getPlayerData().EffectManager.addEffect(EffectType.valueOf(effectName), time)
        } catch (e: Exception) {
            player.sendMessage("§c/getEffect <effect> [<time=200>]")
        }
    }
}
