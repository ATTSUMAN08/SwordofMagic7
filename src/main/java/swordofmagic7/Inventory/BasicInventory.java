package swordofmagic7.Inventory;

import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Sound.CustomSound;
import swordofmagic7.Sound.SoundList;

public class BasicInventory {
    public final Player player;
    public final PlayerData playerData;

    public String wordSearch;

    int ScrollTick = 0;

    public BasicInventory(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    void setScrollTick(int tick) {
        this.ScrollTick = tick;
    }
    public void upScrollTick() {
        if (ScrollTick > 0) {
            this.ScrollTick--;
            CustomSound.playSound(player, SoundList.Click);
        }
    }
    public void downScrollTick(int size) {
        double scroll = size/8f;
        if (ScrollTick+3 < scroll) {
            this.ScrollTick++;
            CustomSound.playSound(player, SoundList.Click);
        }
        else if (ScrollTick > scroll) this.ScrollTick = (int) Math.floor(scroll);
    }
}