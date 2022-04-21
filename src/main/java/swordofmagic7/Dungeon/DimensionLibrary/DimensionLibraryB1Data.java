package swordofmagic7.Dungeon.DimensionLibrary;

import org.bukkit.entity.Player;
import swordofmagic7.Function;

import java.util.HashSet;
import java.util.Set;

public class DimensionLibraryB1Data {
    private static final int split = 25;
    public final Player player;
    public int[] root = new int[16];
    public int progress = 0;

    public DimensionLibraryB1Data(Player player) {
        this.player = player;
        int hash = Function.StringToHashInt(player.getName(), split);
        Set<Integer> used = new HashSet<>();
        for (int i = 0; i < root.length; i++) {
            root[i] = hash;
            used.add(hash);
            hash = Function.StringToHashInt((i^2) + player.getName() + (i^2), split);
            if (used.contains(hash)) {
                for (int x = 0; x < split; x++) {
                    if (!used.contains(x)) hash = x;
                }
            }
        }
    }
}
