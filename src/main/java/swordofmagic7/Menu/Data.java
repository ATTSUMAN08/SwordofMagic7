package swordofmagic7.Menu;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Item.ItemStackData;

import static swordofmagic7.Function.decoText;

public record Data() {
    static final String UserMenuDisplay = "§lユーザーメニュー";
    static final ItemStack UserMenu_ItemInventory = new ItemStackData(Material.CHEST, decoText("アイテムインベントリ"), "§a§lインベントリ表示を[アイテムインベントリ]に切り替えます").view();
    static final ItemStack UserMenu_RuneInventory = new ItemStackData(Material.ENDER_CHEST, decoText("ルーンインベントリ"), "§a§lインベントリ表示を[ルーンインベントリ]に切り替えます").view();
    static final ItemStack UserMenu_PetInventory = new ItemStackData(Material.NOTE_BLOCK, decoText("ペットケージ"), "§a§lインベントリ表示を[ペットケージ]に切り替えます").view();
    static final ItemStack UserMenu_HotBar = new ItemStackData(Material.ITEM_FRAME, decoText("ホットバー"), "§a§lインベントリ表示を[ホットバー]に切り替えます").view();
    static final ItemStack UserMenu_SkillMenuIcon = new ItemStackData(Material.ENCHANTED_BOOK, decoText("スキルメニュー"), "§a§lスキルメニューを開きます").view();
    static final ItemStack UserMenu_RuneMenuIcon = new ItemStackData(Material.PAPER, decoText("ルーンメニュー"), "§a§lルーンメニューを開きます").view();
    static final ItemStack UserMenu_TriggerMenuIcon = new ItemStackData(Material.COMPARATOR, decoText("トリガーメニュー"), "§a§lトリガーメニューを開きます").view();
    static final ItemStack UserMenu_AttributeMenuIcon = new ItemStackData(Material.RED_DYE, decoText("アトリビュートメニュー"), "§a§lアトリビュートメニューを開きます").view();
    static final ItemStack UserMenu_StatusInfoIcon = new ItemStackData(Material.PAINTING, decoText("ステータス情報"), "§a§lステータス情報を開きます").view();
    static final ItemStack UserMenu_SettingMenuIcon = new ItemStackData(Material.CRAFTING_TABLE, decoText("設定メニュー"), "§a§l設定メニューを開きます").view();

    public static final String SkillMenuDisplay = "§lスキルメニュー";

    static final String TriggerMenuDisplay = "§lトリガーメニュー";
    static final ItemStack TriggerMenu_Reset = new ItemStackData(Material.BARRIER, "§c§lスロットを空にする").view();

    static final String StatusInfoDisplay = "§lステータス情報";

    public static final String AttributeMenuDisplay = "§lアトリビュートメニュー";

    public static final String RuneMenuDisplay = "§lルーンメニュー";
    public static final String RuneEquipMenuDisplay = "§lルーン装着";
    public static final String RuneCrushMenuDisplay = "§lルーン粉砕";

    static final String SettingMenuDisplay = "§l設定メニュー";
    static final ItemStack SettingMenu_DamageLogIcon = new ItemStackData(Material.RED_DYE, decoText("ダメージログ"), "§a§lダメージログ表記を切り替えます").view();
    static final ItemStack SettingMenu_ExpLogIcon = new ItemStackData(Material.EXPERIENCE_BOTTLE, decoText("経験値ログ"), "§a§l経験値ログ表記を切り替えます").view();
    static final ItemStack SettingMenu_DropLogIcon = new ItemStackData(Material.CHEST, decoText("ドロップログ"), "§a§lドロップログ表記を切り替えます").view();
    static final ItemStack SettingMenu_StrafeModeIcon = new ItemStackData(Material.FEATHER, decoText("ストレイフモード"), "§a§lストレイフの発動条件を切り替えます").view();
    static final ItemStack SettingMenu_CastModeIcon = new ItemStackData(Material.END_CRYSTAL, decoText("キャストモード"), "§a§lスキルの発動方法を切り替えます").view();
    static final ItemStack SettingMenu_PvPModeIcon = new ItemStackData(Material.IRON_SWORD, decoText("PvPモード"), "§a§lPvPモードを切り替えます").view();
    static final ItemStack SettingMenu_ShopAmountResetIcon = new ItemStackData(Material.GOLD_NUGGET, decoText("ショップ購入数初期化"), "§a§l[ショップ/買取屋]を開くたびに[購入数/売却数]を\n§a§lリセットするか切り替えます").view();

    public static final String PetShopDisplay = "§lペットショップ";

    public static final String UpgradeDisplay = "§l装備強化";

}
