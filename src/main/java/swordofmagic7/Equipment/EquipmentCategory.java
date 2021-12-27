package swordofmagic7.Equipment;

import org.bukkit.Material;

public enum EquipmentCategory {
    Blade("刃剣", Material.STONE_SWORD),
    Hammer("大槌", Material.STONE_AXE),
    Rod("法杖", Material.STONE_HOE),
    ActGun("法銃", Material.GOLDEN_HOE),
    Shield("盾", Material.SHIELD),
    Baton("指揮杖", Material.BLAZE_ROD),
    Armor("アーマー", Material.IRON_CHESTPLATE),
    ;
    public String Display;
    public Material material;

    EquipmentCategory(String Display, Material material) {
        this.Display = Display;
        this.material = material;
    }

    public EquipmentCategory getEquipmentCategory(String str) {
        for (EquipmentCategory loop : EquipmentCategory.values()) {
            if (loop.toString().equalsIgnoreCase(str)) {
                return loop;
            }
        }
        return EquipmentCategory.Blade;
    }
}