package swordofmagic7;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import static swordofmagic7.System.plugin;

public class Demo {
    public void run(LivingEntity entity, int stopCMD, int moveCMD) {
        new BukkitRunnable() {
            Location lastLocation = entity.getLocation();
            @Override
            public void run() {
                int CustomModelData = stopCMD;
                if (lastLocation.distance(entity.getLocation()) > 0.1) {
                    lastLocation = entity.getLocation();
                    CustomModelData = moveCMD;
                }
                if (entity.getEquipment() != null && entity.getEquipment().getHelmet() != null) {
                    ItemStack item = entity.getEquipment().getHelmet();
                    ItemMeta meta = item.getItemMeta();
                    meta.setCustomModelData(CustomModelData);
                    item.setItemMeta(meta);
                }
            }
        }.runTaskTimer(plugin, 0, 5);
    }
}
