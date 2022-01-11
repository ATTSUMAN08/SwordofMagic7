package swordofmagic7.Equipment;

import org.bukkit.Material;

public enum EquipmentCategory {
    Blade("刃剣", Material.STONE_SWORD),
    Mace("鈍器", Material.STONE_SHOVEL),
    Rod("法杖", Material.STONE_HOE),
    ActGun("法銃", Material.GOLDEN_HOE),
    Shield("盾", Material.SHIELD),
    Baton("指揮杖", Material.BLAZE_ROD),
    Armor("アーマー", Material.IRON_CHESTPLATE),
    Trinket("武器装飾", Material.SOUL_LANTERN),
    ;
    public String Display;
    public Material material;

    EquipmentCategory(String Display, Material material) {
        this.Display = Display;
        this.material = material;
    }

    public static EquipmentCategory getEquipmentCategory(String str) {
        for (EquipmentCategory loop : EquipmentCategory.values()) {
            if (loop.toString().equalsIgnoreCase(str)) {
                return loop;
            }
        }
        return EquipmentCategory.Blade;
    }
}