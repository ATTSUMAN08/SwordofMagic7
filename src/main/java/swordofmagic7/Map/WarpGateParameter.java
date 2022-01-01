package swordofmagic7.Map;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import swordofmagic7.Dungeon.AusMine;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Sound.SoundList;

import java.lang.reflect.Method;

import static swordofmagic7.Data.DataBase.AirItem;
import static swordofmagic7.Data.DataBase.WarpGateList;
import static swordofmagic7.Function.Log;
import static swordofmagic7.Particle.ParticleManager.spawnParticle;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.BTTSet;
import static swordofmagic7.System.plugin;

public class WarpGateParameter {
    public String Id;
    public Location Location;
    public String Target;
    public Location TargetLocation;
    public MapData NextMap;
    public boolean isActive = true;
    public String Trigger;

    public void usePlayer(Player player) {
        if (Trigger != null) {
            if (Trigger.equals("AusMineB1") && AusMine.AusMineB1()) return;
        }
        NextMap.enter(player);
        if (Target != null) TargetLocation = WarpGateList.get(Target).Location;
        player.teleportAsync(TargetLocation);
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            playSound(player, SoundList.Warp);
        }, 1);
    }

    public void Active() {
        isActive = true;
        particleData = new ParticleData(Particle.SPELL_WITCH);
    }

    public void ActiveAtTime(int time) {
        Active();
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this::Disable, time);
    }

    public void Disable() {
        isActive = false;
        particleData = new ParticleData(Particle.REDSTONE);
    }

    private World world;
    private ParticleData particleData = new ParticleData(Particle.SPELL_WITCH);
    public void start() {
        Hologram hologram = HologramsAPI.createHologram(plugin, Location.clone().add(0, 4, 0));
        hologram.appendTextLine(NextMap.Color + "§l《" + NextMap.Display + "》");
        hologram.appendTextLine("");
        hologram.appendTextLine(NextMap.Color + "§l推奨Lv" + NextMap.Level);
        world = Location.getWorld();
        BTTSet(new BukkitRunnable() {
            int i = 0;
            final double increment = (2 * Math.PI) / 90;
            final double radius = 2;
            @Override
            public void run() {
                for (int loop = 0; loop < 3; loop++) {
                    i++;
                    double angle = i * increment;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    Location nLoc = new Location(world, Location.getX() + x, Location.getY(), Location.getZ() + z);
                    Location nLoc2 = new Location(world, Location.getX() - x, Location.getY(), Location.getZ() - z);
                    spawnParticle(particleData, nLoc);
                    spawnParticle(particleData, nLoc2);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0 , 1), "WarpGate:" + Id);
    }
}
