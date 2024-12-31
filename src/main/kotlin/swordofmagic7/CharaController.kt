package swordofmagic7

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import swordofmagic7.Data.PlayerData
import swordofmagic7.MultiThread.runTask

import swordofmagic7.Data.PlayerData.playerData
import swordofmagic7.Function.inAir

object CharaController {

    fun WallKick(player: Player) {
        runTask({
            val playerData = playerData(player)
            if (!playerData.EffectManager.isCrowdControl) {
                val loc = player.location
                loc.pitch = 0f
                val type = loc.clone().add(loc.direction.setY(0.1).normalize()).block.type
                if (!player.isFlying && inAir(player)
                    && type.isSolid && type != Material.BARRIER) {
                    playerData.WallKickedTask?.cancel()
                    player.velocity = loc.direction.normalize().multiply(-0.7).setY(0.6)
                    playerData.WallKicked = true
                    playerData.Strafe = 2
                    playerData.statistics.WallJumpCount++
                    player.allowFlight = true
                }
            }
        }, "WallKick")
    }

    fun Strafe(player: Player) {
        runTask({
            val playerData = playerData(player)
            if (!playerData.EffectManager.isCrowdControl) {
                if (!player.isFlying && inAir(player) && playerData.Strafe > 0) {
                    playerData.Strafe--
                    val y: Double
                    if (playerData.WallKicked) {
                        y = 0.7
                        playerData.WallKicked = false
                        playerData.WallKickedTask?.cancel()
                        playerData.WallKickedTask = null
                    } else {
                        y = player.velocity.y
                        if (y < 0) y /= 3
                    }
                    val boost = 0.5 + playerData.Status.Movement / 1.2
                    player.velocity = player.location.direction.setY(0).normalize().multiply(boost).setY(y + playerData.Status.Movement / 2.4 - 0.1)
                    playerData.statistics.StrafeCount++
                }
            }
        }, "Strafe")
    }
}
