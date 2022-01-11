package swordofmagic7.Renderer;

import org.bukkit.entity.Player;

public class MapCustom {

    public static void getCustomMap(Player player, int id) {
        CustomMapData data = new CustomMapData(CustomMapType.Image,"ボムさんに威力を求めるのは間違っているだろうか.png");
        player.getInventory().addItem(new MapRender(data).view(0));
    }

}
