package swordofmagic7;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.StrafeType;
import swordofmagic7.MultiThread.MultiThread;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.inAir;

public class CharaController {

    static void WallKick(Player player) {
        PlayerData playerData = playerData(player);
        if (playerData.StrafeMode == StrafeType.NONE) return;
        MultiThread.TaskRun(() -> {
            if (!playerData.EffectManager.isCrowdControl) {
                Location loc = player.getLocation();
                loc.setPitch(0);
                Material type = loc.clone().add(loc.getDirection().setY(0.1).normalize()).getBlock().getType();
                if (!player.isFlying() && inAir(player)
                        && type.isSolid() && type != Material.BARRIER) {
                    if (playerData.WallKickedTask != null) playerData.WallKickedTask.cancel();
                    player.setVelocity(loc.getDirection().normalize().multiply(-0.7).setY(0.6));
                    playerData.WallKicked = true;
                    playerData.Strafe = 2;
                    playerData.statistics.WallJumpCount++;
                    player.setAllowFlight(true);
                }
            }
        }, "WallKick");
    }

    static void Strafe(Player player) {
        MultiThread.TaskRun(() -> {
            PlayerData playerData = playerData(player);
            if (!playerData.EffectManager.isCrowdControl) {
                if (!player.isFlying() && inAir(player) && playerData.Strafe > 0) {
                    playerData.Strafe--;
                    double y;
                    if (playerData.WallKicked) {
                        y = 0.7;
                        playerData.WallKicked = false;
                        if (playerData.WallKickedTask != null) {
                            playerData.WallKickedTask.cancel();
                            playerData.WallKickedTask = null;
                        }
                    } else {
                        y = player.getVelocity().getY();
                        if (y < 0) y /= 3;
                    }
                    double boost = 0.5 + playerData.Status.Movement/1.2;
                    y += playerData.Status.Movement/2.4-0.1;
                    player.setVelocity(player.getLocation().getDirection().setY(0).normalize().multiply(boost).setY(y));
                    playerData.statistics.StrafeCount++;
                }
            }
        }, "Strafe");
    }
}
