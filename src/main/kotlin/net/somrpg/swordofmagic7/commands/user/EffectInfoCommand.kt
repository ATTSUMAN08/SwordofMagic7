@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.user

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import org.bukkit.entity.Player
import swordofmagic7.Effect.EffectType
import swordofmagic7.Function

@CommandAlias("effectInfo|ei")
@CommandPermission("som7.user")
class EffectInfoCommand : BaseCommand() {
    @Default
    @Syntax("<effectName>")
    @CommandCompletion("@effectDisplayNames")
    fun default(
        player: Player,
        effectName: String,
    ) {
        val effectType =
            EffectType.entries.firstOrNull { it.Display == effectName } ?: run {
                Function.sendMessage(player, "§e/effectInfo <効果名>")
                return
            }
        val message = mutableListOf<String>()
        message.add(Function.decoText(effectType.Display))
        message.add(Function.decoLore("効果タイプ") + (if (effectType.Buff) "§b§lバフ" else "§c§lデバフ"))
        message.add(Function.decoLore("最大スタック") + effectType.MaxStack)
        message.add(Function.decoLore("解除等級") + effectType.effectRank.Display)
        message.add(Function.decoLore("干渉系影響") + (if (effectType.isStatic) "§b§lする" else "§c§lしない"))
        message.add(Function.decoLore("ステータス更新") + (if (effectType.isUpdateStatus) "§b§lする" else "§c§lしない"))
        message.add(Function.decoText("効果説明"))
        for (str in effectType.Lore) {
            message.add("§a$str")
        }
        Function.sendMessage(player, message)
    }
}
