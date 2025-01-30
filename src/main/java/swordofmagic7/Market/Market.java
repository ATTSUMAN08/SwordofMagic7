package swordofmagic7.Market;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Function;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.ItemStackData;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.Shop.Shop.ItemFlame;
import static swordofmagic7.Shop.Shop.ItemFlameAmount;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Market {

    public static void marketCommand(PlayerData playerData, String[] args) {
        Player player = playerData.player;
        if (!playerData.Map.Safe) {
            Function.sendMessage(player, "§eセーフゾーン§aでのみ使用可能です", SoundList.NOPE);
            return;
        }
        if (args.length >= 1) {
            String type = args[0];
            if (type.equalsIgnoreCase("buy")) {
                playerData.Menu.Market.MarketMenuView();
                return;
            } else if (type.equalsIgnoreCase("collect")) {
                MarketContainer market = MarketContainer.getMarket(player.getUniqueId());
                List<String> message = new ArrayList<>();
                int reqMel = (int) Math.ceil(market.Mel*0.05);
                int addMel = market.Mel-reqMel;
                message.add(decoText("取引所 - 売上情報"));
                message.add(decoLore("売上") + market.Mel);
                message.add(decoLore("利益") + addMel);
                message.add(decoLore("手数料") + reqMel);
                Function.sendMessage(player, message);
                if (args.length >= 2 && args[1].equalsIgnoreCase("confirm")) {
                    playerData.Mel += addMel;
                    Function.sendMessage(player, "§b[+]§e" + addMel + "メル§", SoundList.LEVEL_UP);
                    market.Mel = 0;
                    market.save();
                } else playSound(player, SoundList.TICK);
                return;
            }
            if (args.length >= 2) {
                if (type.equalsIgnoreCase("price")) {
                    File file = new File(DataBasePath, "Market/" + MarketPriceYml);
                    FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                    if (ItemList.containsKey(args[1])) {
                        sendMessage(player, "§7・§e" + args[1] + "§7: §a" + (data.isSet(args[1]) ? data.getInt(args[1]) + "メル" : "§7過去取引無し"));
                    } else {
                        sendMessage(player, "§a存在しない§eアイテム§aです", SoundList.NOPE);
                    }
                    return;
                }
                try {
                    int index = Integer.parseInt(args[1]);
                    if (index > -1 && type.equalsIgnoreCase("sell") && playerData.ItemInventory.getList().size() > index) {
                        MarketContainer market = MarketContainer.getMarket(player.getUniqueId());
                        if (market.marketData.size() < 45) {
                            ItemParameterStack stack = playerData.ItemInventory.getItemParameterStack(index).clone();
                            if (stack != null) {
                                int Mel = stack.itemParameter.Sell;
                                if (args.length >= 3) {
                                    int amount = Integer.parseInt(args[2]);
                                    if (amount > 0 && stack.Amount >= amount) {
                                        stack.Amount = amount;
                                    } else {
                                        Function.sendMessage(player, "§a所持数以上は出品できません", SoundList.NOPE);
                                        return;
                                    }
                                }
                                if (args.length >= 4) {
                                    Mel = Integer.parseInt(args[3]);
                                }
                                if (Mel < 1) Mel = 1;
                                int reqMel = (int) Math.ceil(Mel * stack.Amount * 0.01);
                                if (playerData.Mel >= reqMel) {
                                    MarketData marketData = new MarketData(stack, Mel, player.getUniqueId(), System.currentTimeMillis());
                                    market.marketData.add(marketData);
                                    playerData.ItemInventory.removeItemParameter(stack);
                                    Function.sendMessage(player, "§e[" + stack.itemParameter.Display + "§ax" + stack.Amount + "§e]§aを§e" + Mel + "メル/個§aで出品しました §e[出品手数料" + reqMel + "]");
                                    playerData.Mel -= reqMel;
                                    playSound(player, SoundList.TICK);
                                    playerData.viewUpdate();
                                    market.save();
                                } else {
                                    Function.sendMessage(player, "§e出品手数料§aが足りません §e[" + reqMel + "メル]", SoundList.NOPE);
                                }
                            } else {
                                Function.sendMessage(player, "§eSlotID§aが不正です", SoundList.NOPE);
                            }
                        } else {
                            Function.sendMessage(player, "§e出品枠§aが一杯です", SoundList.NOPE);
                        }
                        return;
                    } else if (index > -1 && type.equalsIgnoreCase("cancel")) {
                        MarketContainer market = MarketContainer.getMarket(player.getUniqueId());
                        if (index < market.marketData.size()) {
                            MarketData marketData = market.marketData.get(index);
                            String itemText = "§e[" + marketData.itemParameterStack.itemParameter.Display + "§ax" + marketData.itemParameterStack.Amount + "§e]";
                            playerData.ItemInventory.addItemParameter(market.marketData.get(index).itemParameterStack);
                            market.marketData.remove(index);
                            Function.sendMessage(player, itemText + "§aを取り下げました", SoundList.TICK);
                            market.save();
                        } else {
                            Function.sendMessage(player, "§e使用中枠: 0 -> " + (market.marketData.size() - 1), SoundList.TICK);
                        }
                        return;
                    } else if (index > -1 && type.equalsIgnoreCase("info")) {
                        MarketContainer market = MarketContainer.getMarket(player.getUniqueId());
                        if (index < market.marketData.size()) {
                            MarketData marketData = market.marketData.get(index);
                            List<String> message = new ArrayList<>();
                            message.add(Function.decoText("出品枠[" + index + "]"));
                            message.add("§7・§e" + marketData.itemParameterStack.itemParameter.Display + "§ax" + marketData.itemParameterStack.Amount);
                            message.add("§7・§e" + marketData.Mel + "メル/個");
                            message.add(Function.decoLore("経過") + (int) Math.floor((System.currentTimeMillis() - marketData.timeStamp)/60000f) + "分");
                            for (String str : message) {
                                player.sendMessage(str);
                            }
                        } else {
                            Function.sendMessage(player, "§e使用中枠: 0 -> " + (market.marketData.size() - 1), SoundList.TICK);
                        }
                        playSound(player, SoundList.TICK);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        player.sendMessage(Function.decoLore("/market buy"));
        player.sendMessage(Function.decoLore("/market sell <SlotID> <個数> <メル/個>"));
        player.sendMessage(Function.decoLore("/market cancel <index>"));
        player.sendMessage(Function.decoLore("/market info <index>"));
        player.sendMessage(Function.decoLore("/market collect [confirm]"));
        player.sendMessage(Function.decoLore("/market price <ItemId>"));
    }

    private final Player player;
    private final PlayerData playerData;
    private final MarketCache[] MarketArray = new MarketCache[45];
    private int page;
    private int BuyAmount = 1;
    private static final String MarketPrefix = "購入";

    public Market(PlayerData playerData) {
        this.playerData = playerData;
        player = playerData.player;
    }

    public String MarketMenuDisplay = "マーケットメニュー";
    public String MarketBuyConfirmDisplay = "マーケット購入確認";

    public ItemStack buyConfirm = new ItemStackData(Material.EMERALD_BLOCK, "§e購入する").view();
    public ItemStack Cancel = new ItemStackData(Material.REDSTONE_BLOCK, "§eキャンセル").view();

    public void MarketMenuView() {
        Inventory inv = decoInv(MarketMenuDisplay, 6);
        player.openInventory(inv);
        playSound(player, SoundList.MENU_OPEN);
        MultiThread.TaskRunLater(() -> MarketMenuView(0), 1, "MarketMenuView");
    }

    public void MarketBuyConfirmView() {
        Inventory inv = decoInv(MarketBuyConfirmDisplay, 1);
        inv.setItem(4, marketCache.marketData.itemParameterStack.itemParameter.viewItem(BuyAmount, playerData.ViewFormat()));
        for (int i = 0; i < 3; i++) {
            inv.setItem(i, buyConfirm);
            inv.setItem(i+6, Cancel);
        }
        player.openInventory(inv);
        playSound(player, SoundList.MENU_OPEN);
    }

    public static final String MarketPriceYml = "MarketPrice.yml";

    public void MarketMenuView(int page) {
        if (equalInv(player.getOpenInventory(), MarketMenuDisplay)) {
            this.page = page;
            ItemStack[] itemStacks = new ItemStack[54];
            List<MarketCache> marketList = new ArrayList<>();
            for (File file : DataBase.dumpFile(new File(DataBasePath, "Market/"))) {
                if (!file.getName().equals(MarketPriceYml)) {
                    UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
                    if (!player.getUniqueId().toString().equals(uuid.toString())) {
                        for (int i = 0; i < MarketContainer.getMarket(uuid).marketData.size(); i++) {
                            MarketData marketData = MarketContainer.getMarket(uuid).marketData.get(i);
                            marketList.add(new MarketCache(marketData, i));
                        }
                    }
                }
            }
            try {
                marketList.sort(new MarketSort());
            } catch (Exception e) {
                sendMessage(player, "§eソート処理中§aに§cエラー§aが発生したため§eソート処理§aを§e中断§aしました §c" + e.getMessage());
            }
            int index = page * 45;
            for (int i = 0; i < 45; i++) {
                if (marketList.size() > index) {
                    MarketData marketData = marketList.get(index).marketData;
                    itemStacks[i] = marketData.view(playerData.ViewFormat());
                    MarketArray[i] = marketList.get(index);
                    index++;
                } else break;
            }
            itemStacks[45] = page > 0 ? PreviousPageItem : ShopFlame;
            itemStacks[46] = ItemFlame(-100);
            itemStacks[47] = ItemFlame(-10);
            itemStacks[48] = ItemFlame(-1);
            itemStacks[50] = ItemFlame(1);
            itemStacks[51] = ItemFlame(10);
            itemStacks[52] = ItemFlame(100);
            itemStacks[53] = page < Math.floor(marketList.size()/45f) ? NextPageItem : ShopFlame;
            itemStacks[49] = ItemFlameAmount(MarketPrefix, BuyAmount);
            MultiThread.TaskRunSynchronized(() -> player.getOpenInventory().getTopInventory().setContents(itemStacks));
            playSound(player, SoundList.TICK);
        }
    }

    public MarketCache marketCache;
    public void MarketMenuClick(InventoryView view, ItemStack currentItem, int Slot) {
        if (equalInv(view, MarketBuyConfirmDisplay)) {
            if (currentItem != null) {
                if (equalItem(currentItem, buyConfirm)) {
                    if (marketCache != null) {
                        MarketContainer market = MarketContainer.getMarket(marketCache.marketData.Owner);
                        int index = marketCache.index;
                        MarketData marketData = market.marketData.get(index);
                        if (!marketCache.marketData.uuid.toString().equals(marketData.uuid.toString())) {
                            Function.sendMessage(player, "§aすでに§e購入§aされているか§e出品§aが取り消されました", SoundList.NOPE);
                            return;
                        }
                        int amount = BuyAmount;
                        if (amount > marketData.itemParameterStack.Amount)
                            amount = marketData.itemParameterStack.Amount;
                        if (playerData.Mel >= marketData.Mel * amount) {
                            ItemParameter itemParameter = marketData.itemParameterStack.itemParameter.clone();
                            playerData.ItemInventory.addItemParameter(itemParameter, amount);
                            player.sendMessage("§e[" + itemParameter.Display + "§ax" + amount + "§e]§aを§e購入§aしました");
                            marketData.itemParameterStack.Amount -= amount;
                            if (marketData.itemParameterStack.Amount <= 0) {
                                market.marketData.remove(index);
                            } else {
                                market.marketData.set(index, marketData);
                            }
                            playerData.Mel -= marketData.Mel * amount;
                            market.Mel += marketData.Mel * amount;
                            market.save();
                            MarketMenuView(page);
                            playSound(player, SoundList.LEVEL_UP);
                            File file = new File(DataBasePath, "Market/" + MarketPriceYml);
                            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                            String id = marketData.itemParameterStack.itemParameter.Id;
                            int price = Math.round((data.getInt(id, marketData.Mel)+marketData.Mel)/2f);
                            data.set(id, price);
                            try {
                                data.save(file);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            MarketMenuView();
                        } else {
                            sendMessage(player, "§eメル§aが足りません", SoundList.NOPE);
                        }
                    } else {
                        Function.sendMessage(player, "§aすでに§e購入§aされているか§e出品§aが取り消されました", SoundList.NOPE);
                    }
                } else if (equalItem(currentItem, Cancel)) {
                    marketCache = null;
                    MarketMenuView();
                }
            }
        } else if (equalInv(view, MarketMenuDisplay)) {
            if (Slot < 45) {
                if (currentItem != null) {
                    if (MarketArray[Slot] != null) {
                        marketCache = MarketArray[Slot];
                        MarketBuyConfirmView();
                    } else MarketMenuView(page);
                }
            } else {
                if (equalItem(currentItem, PreviousPageItem)) {
                    page--;
                    MarketMenuView(page);
                } else if (equalItem(currentItem, NextPageItem)) {
                    page++;
                    MarketMenuView(page);
                } else {
                    switch (Slot) {
                        case 46 -> BuyAmount -= 100;
                        case 47 -> BuyAmount -= 10;
                        case 48 -> BuyAmount--;
                        case 50 -> BuyAmount++;
                        case 51 -> BuyAmount += 10;
                        case 52 -> BuyAmount += 100;
                    }
                    if (BuyAmount < 1) BuyAmount = 1;
                    if (BuyAmount > 10000) BuyAmount = 10000;
                    playSound(player, SoundList.CLICK);
                }
                view.getTopInventory().setItem(49, ItemFlameAmount(MarketPrefix, BuyAmount));
            }
        }
    }
}

class MarketCache {
    MarketData marketData;
    int index;

    MarketCache(MarketData marketData, int index) {
        this.marketData = marketData;
        this.index = index;
    }
}

class MarketSort implements Comparator<MarketCache> {
    public int compare(MarketCache market, MarketCache market2) {
        ItemParameter item = market.marketData.itemParameterStack.itemParameter;
        ItemParameter item2 = market2.marketData.itemParameterStack.itemParameter;
        if (item == null || item2 == null) return 0;
        if (item.Category == item2.Category) {
            return item.Id.compareTo(item2.Id);
        } else {
            return item.Category.compareTo(item2.Category);
        }
    }
}