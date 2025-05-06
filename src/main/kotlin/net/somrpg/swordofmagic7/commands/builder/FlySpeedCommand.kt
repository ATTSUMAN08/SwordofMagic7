@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.builder

import me.attsuman08.abysslib.shade.acf.BaseCommand
import me.attsuman08.abysslib.shade.acf.annotation.CommandAlias
import me.attsuman08.abysslib.shade.acf.annotation.CommandPermission
import me.attsuman08.abysslib.shade.acf.annotation.Conditions
import me.attsuman08.abysslib.shade.acf.annotation.Default
import me.attsuman08.abysslib.shade.acf.annotation.Syntax
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

@CommandAlias("flyspeed|fs")
@CommandPermission("som7.builder")
class FlySpeedCommand : BaseCommand() {

    @Default
    @Syntax("<speed>")
    fun flySpeed(sender: Player, @Conditions("limits:min=0.1,max=1") @Default("0.1") speed: Float) {
        sender.flySpeed = speed
        sender.sendMessage(Component.text("FlySpeedを${speed}に設定しました"))
    }

}