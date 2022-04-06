package swordofmagic7.Dungeon.Tarnet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.SomCore.plugin;

public class TarnetEnter {

    static World world = Bukkit.getWorld("world");
    static Location EventLocation = new Location(world, 3109, 130, 750);
    static Location EventPos1 = new Location(world, 3106, 109, 746);
    static Location EventPos2 = new Location(world, 3112, 115, 742);
    static Location AreaPos1 = new Location(world, 3111, 136, 741);
    static Location AreaPos2 = new Location(world, 3106, 130, 749);
    static List<Block> blocks = new ArrayList<>();
    static boolean inProgress = false;

    public static void start() {
        if (!inProgress) {
            inProgress = true;
            MultiThread.TaskRun(() -> {
                int i = 3;
                while (plugin.isEnabled() && i > 0 && inProgress) {
                    set(i);
                    i--;
                    MultiThread.sleepTick(20);
                }
                MultiThread.sleepTick(600);
                reset();
                inProgress = false;
            }, "TarnetEnter");
        }
    }

    public static void reset() {
        MultiThread.TaskRunSynchronized(() -> {
            int x1 = Math.min(AreaPos1.getBlockX(), AreaPos2.getBlockX());
            int x2 = Math.max(AreaPos1.getBlockX(), AreaPos2.getBlockX());
            int y1 = Math.min(AreaPos1.getBlockY(), AreaPos2.getBlockY());
            int y2 = Math.max(AreaPos1.getBlockY(), AreaPos2.getBlockY());
            int z1 = Math.min(AreaPos1.getBlockZ(), AreaPos2.getBlockZ());
            int z2 = Math.max(AreaPos1.getBlockZ(), AreaPos2.getBlockZ());
            for (int x = x1; x < x2; x++) {
                for (int y = y1; y < y2; y++) {
                    for (int z = z1; z < z2; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        block.setBlockData(world.getBlockAt(x, y, z).getBlockData());
                        block.setType(Material.AIR);
                    }
                }
            }
            set(4);
        });
    }

    static void set(int Progress) {
        MultiThread.TaskRunSynchronized(() -> {
            for (Block block : blocks) {
                block.setType(Material.AIR);
            }
            blocks.clear();
            int x1 = Math.min(EventPos1.getBlockX(),EventPos2.getBlockX());
            int x2 = Math.max(EventPos1.getBlockX(),EventPos2.getBlockX());
            int y1 = Math.min(EventPos1.getBlockY(),EventPos2.getBlockY());
            int y2 = Math.max(EventPos1.getBlockY(),EventPos2.getBlockY());
            int z1 = Math.min(EventPos1.getBlockZ(),EventPos2.getBlockZ());
            int z2 = Math.max(EventPos1.getBlockZ(),EventPos2.getBlockZ());
            for (int x = x1; x < x2; x++) {
                for (int y = y1; y < y2; y++) {
                    for (int z = z1; z < z2; z++) {
                        Block block = world.getBlockAt(x, y+21, z+Progress);
                        block.setBlockData(world.getBlockAt(x, y, z).getBlockData());
                        blocks.add(block);
                    }
                }
            }
        playSound(EventLocation, SoundList.Rock);
        });
    }
}
