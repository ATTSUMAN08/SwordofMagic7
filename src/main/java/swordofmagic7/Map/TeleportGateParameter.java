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
import static net.somrpg.swordofmagic7.SomCore.instance;

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
    }

    public ParticleData getParticleData() {
        return particleData;
    }
}
