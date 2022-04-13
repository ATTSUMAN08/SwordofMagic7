package swordofmagic7.Map;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Dungeon.AusMine.AusMineB1;
import swordofmagic7.Dungeon.AusMine.AusMineB2;
import swordofmagic7.Dungeon.AusMine.AusMineB3;
import swordofmagic7.Dungeon.AusMine.AusMineB4;
import swordofmagic7.Dungeon.Novaha.Novaha2;
import swordofmagic7.Dungeon.Novaha.Novaha3;
import swordofmagic7.Dungeon.Novaha.Novaha4;
import swordofmagic7.Dungeon.Tarnet.TarnetB1;
import swordofmagic7.Dungeon.Tarnet.TarnetB3;
import swordofmagic7.Function;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Data.DataBase.WarpGateList;
import static swordofmagic7.Particle.ParticleManager.spawnParticle;
import static swordofmagic7.SomCore.createHologram;
import static swordofmagic7.SomCore.plugin;
import static swordofmagic7.Sound.CustomSound.playSound;

public class WarpGateParameter {
    public String Id;
    private Location Location;
    public String Target;
    public Location TargetLocation;
    public MapData NextMap;
    public boolean isActive = true;
    public String Trigger;

    public void usePlayer(Player player) {
        if (NextMap.ReqCombatPower > PlayerData.playerData(player).Status.getCombatPower()) {
            Function.sendMessage(player, "§e[戦闘力]§aが足りません §c[必要戦闘力:" + NextMap.ReqCombatPower + "]", SoundList.Nope);
            return;
        }
        if (Trigger != null) {
            if (Trigger.equals("AusMineB1") && AusMineB1.Start()) return;
            if (Trigger.equals("AusMineB2") && AusMineB2.Start()) return;
            if (Trigger.equals("AusMineB3") && AusMineB3.Start()) return;
            if (Trigger.equals("AusMineB4") && AusMineB4.Start()) return;
            if (Trigger.equals("TarnetB1")) TarnetB1.Start();
            if (Trigger.equals("TarnetB3")) TarnetB3.Start();
            if (Trigger.equals("NovahaMiddleBoss")) Novaha2.Start();
            if (Trigger.equals("Novaha3")) if (isActive) Novaha3.Start(); else return;
            if (Trigger.equals("Novaha4")) if (isActive) Novaha4.Start(); else {
                Novaha3.Start();
                return;
            }
        } else if (!isActive) return;
        NextMap.enter(player);
        if (Target != null) TargetLocation = WarpGateList.get(Target).getLocation();
        player.teleportAsync(TargetLocation);
        MultiThread.TaskRun(() -> {
            MultiThread.sleepTick(1);
            playSound(player, SoundList.Warp);
        }, "WarpGateTeleport");
    }

    public void setLocation(org.bukkit.Location location) {
        Location = location;
    }

    public org.bukkit.Location getLocation() {
        return Location.clone();
    }

    public void Active() {
        isActive = true;
        particleData = new ParticleData(Particle.SPELL_WITCH);
    }

    public void ActiveAtTime(int time) {
        Active();
        MultiThread.TaskRun(() -> {
            MultiThread.sleepTick(time);
            Disable();
        }, "ActiveAtTime: " + Id);
    }

    public void Disable() {
        isActive = false;
        particleData = new ParticleData(Particle.REDSTONE);
    }

    private World world;
    private ParticleData particleData = new ParticleData(Particle.SPELL_WITCH);
    private boolean isStarted = false;
    public void start() {
        if (isStarted) return;
        isStarted = true;
        Hologram hologram = createHologram(getLocation().add(0, 4, 0));
        hologram.appendTextLine(NextMap.Color + "§l《" + NextMap.Display + "》");
        hologram.appendTextLine("");
        hologram.appendTextLine(NextMap.Color + "§c必要戦闘力 " + String.format("%.0f", NextMap.ReqCombatPower));
        world = getLocation().getWorld();
        MultiThread.TaskRun(() -> {
            int i = 0;
            final double increment = (2 * Math.PI) / 90;
            final double radius = 2;
            while (plugin.isEnabled()) {
                double angle = i * increment;
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                Location nLoc = new Location(world, Location.getX() + x, Location.getY(), Location.getZ() + z);
                Location nLoc2 = new Location(world, Location.getX() - x, Location.getY(), Location.getZ() - z);
                spawnParticle(particleData, nLoc);
                spawnParticle(particleData, nLoc2);
                i++;
                MultiThread.sleepMillis(10);
            }
        }, "WarpGate");
    }
}
