package swordofmagic7;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.MultiThread.MultiThread;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.inAir;

public class CharaController {

    static void WallKick(Player player) {
        PlayerData playerData = playerData(player);
        Location loc = player.getLocation();
        loc.setPitch(0);
        Material type = loc.clone().add(loc.getDirection().setY(0.1).normalize()).getBlock().getType();
        if (!player.isFlying() && player.isSneaking() && inAir(player)
                && type.isSolid() && type != Material.BARRIER) {
            if (playerData.WallKickedTask != null) playerData.WallKickedTask.cancel();
            player.setVelocity(loc.getDirection().normalize().multiply(-0.7).setY(0.6));
            playerData.WallKicked = true;
            playerData.Strafe = 2;
            playerData.statistics.WallJumpCount++;
            player.setAllowFlight(true);
            MultiThread.TaskRun(() -> {
                MultiThread.sleepTick(12);
                playerData.WallKicked = false;
            }, "WallKick: " + player.getName());
        }
    }

    static void Strafe(Player player) {
        PlayerData playerData = playerData(player);
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
                if (y < 0) y /=2;
            }
            player.setVelocity(player.getLocation().getDirection().setY(0).normalize().multiply(0.7).setY(y));
            playerData.statistics.StrafeCount++;
        }
    }
}
