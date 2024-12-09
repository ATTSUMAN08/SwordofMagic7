@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.builder

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Default
import org.incendo.cloud.annotations.Permission

class FlySpeedCommand {

    @Command("flyspeed|fs [speed]")
    @Permission("som7.builder")
    fun flySpeed(sender: CommandSender, @Default("0.2") @Argument("speed") speed: Float) {
        if (sender !is Player) {
            sender.sendMessage("このコマンドはプレイヤー専用です")
            return
        }

        sender.flySpeed = speed
        sender.sendMessage(Component.text("FlySpeedを${speed}に設定しました"))
    }

}