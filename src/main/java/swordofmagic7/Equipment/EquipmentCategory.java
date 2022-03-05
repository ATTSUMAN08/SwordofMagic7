package swordofmagic7.Equipment;

import org.bukkit.Material;

public enum EquipmentCategory {
    Blade("刃剣", "ブレード", Material.STONE_SWORD),
    Mace("鈍器", "メイス", Material.STONE_SHOVEL),
    Rod("法杖", "ロッド", Material.STONE_HOE),
    ActGun("法銃", "アクトガン", Material.GOLDEN_HOE),
    Shield("盾", "シールド", Material.SHIELD),
    Baton("指揮杖", "バトン", Material.BLAZE_ROD),
    Armor("鎧", "アーマー", Material.IRON_CHESTPLATE),
    Trinket("武器装飾", "トリンケット", Material.SOUL_LANTERN),
    ;
    public String Display;
    public String Display2;
    public Material material;

    EquipmentCategory(String Display, String Display2, Material material) {
        this.Display = Display;
        this.Display2 = Display2;
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