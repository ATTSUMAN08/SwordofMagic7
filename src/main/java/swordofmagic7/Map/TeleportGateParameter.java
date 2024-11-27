package swordofmagic7.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Item.ItemStackData;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Function.*;
import static swordofmagic7.Particle.ParticleManager.spawnParticle;
import static swordofmagic7.SomCore.plugin;

public class TeleportGateParameter {
    public String Id;
    public String Display;
    public Material Icon;
    public String Title;
    public String Subtitle;
    public Location Location;
    public boolean DefaultActive;
    public MapData Map;
    public int Mel = 0;

    public ItemStack view() {
        List<String> lore = new ArrayList<>();
        lore.add(decoLore("転移費") + Mel + "メル");
        lore.add(decoLore("転移先レベル") + Map.Level);
        lore.add(decoLore("転移先マップ") + Map.Display);
        return new ItemStackData(Icon, decoText(Display), lore).view();
    }

    private World world;
    private final ParticleData particleData = new ParticleData(Particle.FIREWORK, 0.1f, VectorUp);
    public void start() {
        world = Location.getWorld();
        MultiThread.TaskRun(() -> {
            int i = 0;
            final double increment = (2 * Math.PI) / 90;
            final double radius = 1.5;
            while (plugin.isEnabled()) {
                i++;
                double angle = i * increment;
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                Location nLoc = new Location(world, Location.getX() + x, Location.getY(), Location.getZ() + z);
                Location nLoc2 = new Location(world, Location.getX() - x, Location.getY(), Location.getZ() - z);
                spawnParticle(particleData, nLoc);
                spawnParticle(particleData, nLoc2);
                MultiThread.sleepMillis(25);
            }
        }, "TeleportGate");
    }
}
