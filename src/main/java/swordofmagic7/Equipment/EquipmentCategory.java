package swordofmagic7.Equipment;

import org.bukkit.Material;

public enum EquipmentCategory {
    Blade("刃剣", "ブレード", Material.STONE_SWORD, EquipmentSlot.MainHand),
    Mace("鈍器", "メイス", Material.STONE_SHOVEL, EquipmentSlot.MainHand),
    Rod("法杖", "ロッド", Material.STONE_HOE, EquipmentSlot.MainHand),
    ActGun("法銃", "アクトガン", Material.GOLDEN_HOE, EquipmentSlot.MainHand),
    Shield("盾", "シールド", Material.IRON_HORSE_ARMOR, EquipmentSlot.OffHand),
    Baton("指揮杖", "バトン", Material.BLAZE_ROD, EquipmentSlot.MainHand),
    Armor("鎧", "アーマー", Material.IRON_CHESTPLATE, EquipmentSlot.Armor),
    Trinket("武器装飾", "トリンケット", Material.MUSIC_DISC_13, EquipmentSlot.OffHand),
    ;
    public String Display;
    public String Display2;
    public EquipmentSlot DefaultEquipmentSlot;
    public Material material;

    EquipmentCategory(String Display, String Display2, Material material, EquipmentSlot equipmentSlot) {
        this.Display = Display;
        this.Display2 = Display2;
        this.DefaultEquipmentSlot = equipmentSlot;
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