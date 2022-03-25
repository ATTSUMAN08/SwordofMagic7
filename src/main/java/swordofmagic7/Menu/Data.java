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
    static final ItemStack UserMenu_HotBar = new ItemStackData(Material.ITEM_FRAME, decoText("スキルスロット"), "§a§lインベントリ表示を[スキルスロット]に切り替えます").view();
    static final ItemStack UserMenu_SkillMenuIcon = new ItemStackData(Material.ENCHANTED_BOOK, decoText("スキルメニュー"), "§a§lスキルメニューを開きます").view();
    static final ItemStack UserMenu_RuneMenuIcon = new ItemStackData(Material.PAPER, decoText("ルーンメニュー"), "§a§lルーンメニューを開きます").view();
    static final ItemStack UserMenu_TriggerMenuIcon = new ItemStackData(Material.COMPARATOR, decoText("トリガーメニュー"), "§a§lトリガーメニューを開きます").view();
    static final ItemStack UserMenu_AttributeMenuIcon = new ItemStackData(Material.RED_DYE, decoText("アトリビュートメニュー"), "§a§lアトリビュートメニューを開きます").view();
    static final ItemStack UserMenu_StatusInfoIcon = new ItemStackData(Material.PAINTING, decoText("ステータス情報"), "§a§lステータス情報を開きます").view();
    static final ItemStack UserMenu_SettingMenuIcon = new ItemStackData(Material.CRAFTING_TABLE, decoText("設定メニュー"), "§a§l設定メニューを開きます").view();
    static final ItemStack UserMenu_TitleMenuIcon = new ItemStackData(Material.OAK_SIGN, decoText("称号一覧"), "§a§l称号一覧を開きます").view();
    static final ItemStack UserMenu_SpawnIcon = new ItemStackData(Material.ENDER_EYE, decoText("帰還"), "§a§lアルデンに帰還します").view();

    public static final String SkillMenuDisplay = "§lスキルメニュー";

    static final String TriggerMenuDisplay = "§lトリガーメニュー";
    static final ItemStack TriggerMenu_Reset = new ItemStackData(Material.BARRIER, "§c§lスロットを空にする").view();

    static final String StatusInfoDisplay = "§lステータス情報";

    public static final String AttributeMenuDisplay = "§lアトリビュートメニュー";

    static final String SettingMenuDisplay = "§l設定メニュー";
    static final ItemStack SettingMenu_DamageLogIcon = new ItemStackData(Material.RED_DYE, decoText("ダメージログ"), "§a§lダメージログ表記を切り替えます").view();
    static final ItemStack SettingMenu_ExpLogIcon = new ItemStackData(Material.EXPERIENCE_BOTTLE, decoText("経験値ログ"), "§a§l経験値ログ表記を切り替えます").view();
    static final ItemStack SettingMenu_DropLogIcon = new ItemStackData(Material.CHEST, decoText("ドロップログ"), "§a§lドロップログ表記を切り替えます").view();
    static final ItemStack SettingMenu_StrafeModeIcon = new ItemStackData(Material.FEATHER, decoText("ストレイフモード"), "§a§lストレイフの発動条件を切り替えます").view();
    static final ItemStack SettingMenu_CastModeIcon = new ItemStackData(Material.END_CRYSTAL, decoText("キャストモード"), "§a§lスキルの発動方法を切り替えます").view();
    static final ItemStack SettingMenu_PvPModeIcon = new ItemStackData(Material.IRON_SWORD, decoText("PvPモード"), "§a§lPvPモードを切り替えます").view();
    static final ItemStack SettingMenu_FishingDisplayNumIcon = new ItemStackData(Material.FISHING_ROD, decoText("釣獲コンボ表記"), "§a§l釣獲コンボ表記を切り替えます\n§c※リソースパックが必要です").view();
    static final ItemStack SettingMenu_FishingUseCombo = new ItemStackData(Material.WATER_BUCKET, decoText("釣獲モード"), "§a§l釣獲モードを切り替えます\n§a§lTA時は自動釣獲が有効になります").view();
    static final ItemStack SettingMenu_HoloSelfViewIcon = new ItemStackData(Material.NAME_TAG, decoText("自視点ステータスバー"), "§a§l自視点ステータスバーの表示を切り替えます").view();
    static final ItemStack SettingMenu_ShopAmountResetIcon = new ItemStackData(Material.GOLD_NUGGET, decoText("ショップ購入数初期化"), "§a§l[ショップ/買取屋]を開くたびに[購入数/売却数]を\n§a§lリセットするか切り替えます").view();
    static final ItemStack SettingMenu_ViewFormat = new ItemStackData(Material.COMMAND_BLOCK, decoText("表示桁数"), "§a§lステータスなどの数値の表示桁数を変更します").view();
    static final ItemStack SettingMenu_ItemInventorySort = new ItemStackData(Material.CHEST, decoText("アイテムインベントリ [ソート方法]"), "§a§lアイテムインベントリのソート方法を変更します").view();
    static final ItemStack SettingMenu_RuneInventorySort = new ItemStackData(Material.ENDER_CHEST, decoText("ルーンインベントリ [ソート方法]"), "§a§lルーンインベントリのソート方法を変更します").view();
    static final ItemStack SettingMenu_PetInventorySort = new ItemStackData(Material.NOTE_BLOCK, decoText("ペットケージ [ソート方法]"), "§a§lペットケージのソート方法を変更します").view();
    static final ItemStack SettingMenu_ItemInventorySortReverse = new ItemStackData(Material.CHEST, decoText("アイテムインベントリ [ソート順]"), "§a§lアイテムインベントリのソート順を変更します").view();
    static final ItemStack SettingMenu_RuneInventorySortReverse = new ItemStackData(Material.ENDER_CHEST, decoText("ルーンインベントリ [ソート順]"), "§a§lルーンインベントリのソート順を変更します").view();
    static final ItemStack SettingMenu_PetInventorySortReverse = new ItemStackData(Material.NOTE_BLOCK, decoText("ペットケージ [ソート順]"), "§a§lペットケージのソート順を変更します").view();

    public static final String UpgradeDisplay = "§l装備強化";

    public static final String SmithMenuDisplay = "§l鍛冶場";
    static final ItemStack SmithMenu_SmeltingIcon = new ItemStackData(Material.FURNACE, decoText("製錬炉"), "§a§l製錬炉メニューを開きます").view();
    static final ItemStack SmithMenu_CreateEquipmentIcon = new ItemStackData(Material.CRAFTING_TABLE, decoText("装備制作"), "§a§l装備制作メニューを開きます").view();
    static final ItemStack SmithMenu_UpgradeEquipmentIcon = new ItemStackData(Material.ANVIL, decoText("装備強化"), "§a§l装備強化メニューを開きます\n§a§l強化石を消費して装備の強化値を上げます\n§a§l強化石は必要量の50%~100%個消費されます").view();
    static final ItemStack SmithMenu_MaterializationIcon = new ItemStackData(Material.TURTLE_EGG, decoText("装備素材化"), "§a§l装備素材化メニューを開きます").view();

    public static String NonMel = "§eメル§aが足りません";

    static final String TitleMenuDisplay = "§l称号一覧";
}
