@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.builder

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import swordofmagic7.Data.PlayerData

@CommandAlias("flyspeed|fs")
@CommandPermission("som7.builder")
class FlySpeedCommand : BaseCommand() {
    @Default
    @Syntax("<speed>")
    fun flySpeed(
        playerData: PlayerData,
        @Conditions("limits:min=0.1,max=1") @Default("0.1") speed: Float,
    ) {
        playerData.player.flySpeed = speed
        playerData.sendRichMessage("FlySpeedを${speed}に設定しました")
    }
}
