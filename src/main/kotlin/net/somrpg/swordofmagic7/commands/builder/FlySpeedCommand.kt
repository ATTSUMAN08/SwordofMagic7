@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.builder

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

@CommandAlias("flyspeed|fs")
@CommandPermission("som7.builder")
class FlySpeedCommand : BaseCommand() {
    @Default
    @Syntax("<speed>")
    fun flySpeed(
        sender: Player,
        @Conditions("limits:min=0.1,max=1") @Default("0.1") speed: Float,
    ) {
        sender.flySpeed = speed
        sender.sendMessage(Component.text("FlySpeedを${speed}に設定しました"))
    }
}
