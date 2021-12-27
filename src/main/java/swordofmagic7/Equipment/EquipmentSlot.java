package swordofmagic7.Equipment;

public enum EquipmentSlot {
    MainHand("メインハンド"),
    OffHand("オフハンド"),
    Armor("アーマー"),
    ;

    public String Display;

    EquipmentSlot(String Display) {
        this.Display = Display;
    }

    public EquipmentSlot getEquipmentSlot(String str) {
        for (EquipmentSlot loop : EquipmentSlot.values()) {
            if (loop.toString().equalsIgnoreCase(str)) {
                return loop;
            }
        }
        return EquipmentSlot.MainHand;
    }
}
