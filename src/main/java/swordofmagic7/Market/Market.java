package swordofmagic7.Market;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Function;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;

import java.io.File;
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
            Function.sendMessage(player, "§eセーフゾーン§aでのみ使用可能です", SoundList.Nope);
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
                    Function.sendMessage(player, "§b[+]§e" + addMel + "メル§", SoundList.LevelUp);
                    market.Mel = 0;
                    market.save();
                } else playSound(player, SoundList.Tick);
                return;
            }
            if (args.length >= 2) {
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
                                        Function.sendMessage(player, "§a所持数以上は出品できません", SoundList.Nope);
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
                                    playSound(player, SoundList.Tick);
                                    playerData.viewUpdate();
                                    market.save();
                                } else {
                                    Function.sendMessage(player, "§e出品手数料§aが足りません §e[" + reqMel + "メル]", SoundList.Nope);
                                }
                            } else {
                                Function.sendMessage(player, "§eSlotID§aが不正です", SoundList.Nope);
                            }
                        } else {
                            Function.sendMessage(player, "§e出品枠§aが一杯です", SoundList.Nope);
                        }
                        return;
                    } else if (index > -1 && type.equalsIgnoreCase("cancel")) {
                        MarketContainer market = MarketContainer.getMarket(player.getUniqueId());
                        if (index < market.marketData.size()) {
                            MarketData marketData = market.marketData.get(index);
                            String itemText = "§e[" + marketData.itemParameterStack.itemParameter.Display + "§ax" + marketData.itemParameterStack.Amount + "§e]";
                            playerData.ItemInventory.addItemParameter(market.marketData.get(index).itemParameterStack);
                            market.marketData.remove(index);
                            Function.sendMessage(player, itemText + "§aを取り下げました", SoundList.Tick);
                            market.save();
                        } else {
                            Function.sendMessage(player, "§e使用中枠: 0 -> " + (market.marketData.size() - 1), SoundList.Tick);
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
                            Function.sendMessage(player, "§e使用中枠: 0 -> " + (market.marketData.size() - 1), SoundList.Tick);
                        }
                        playSound(player, SoundList.Tick);
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

    public void MarketMenuView() {
        Inventory inv = decoInv(MarketMenuDisplay, 6);
        player.openInventory(inv);
        playSound(player, SoundList.MenuOpen);
        MultiThread.TaskRunLater(() -> MarketMenuView(0), 1, "MarketMenuView: " + player.getName());
    }

    public void MarketMenuView(int page) {
        if (equalInv(player.getOpenInventory(), MarketMenuDisplay)) {
            this.page = page;
            ItemStack[] itemStacks = new ItemStack[54];
            List<MarketCache> marketList = new ArrayList<>();
            for (File file : DataBase.dumpFile(new File(DataBasePath, "Market/"))) {
                UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
                if (!player.getUniqueId().toString().equals(uuid.toString())) {
                    for (int i = 0; i < MarketContainer.getMarket(uuid).marketData.size(); i++) {
                        MarketData marketData = MarketContainer.getMarket(uuid).marketData.get(i);
                        marketList.add(new MarketCache(marketData, i));
                    }
                }
            }
            marketList.sort(new MarketSort());
            int index = page * 45;
            for (int i = 0; i < 45; i++) {
                if (marketList.size() > index) {
                    MarketData marketData = marketList.get(index).marketData;
                    itemStacks[i] = marketData.view(playerData.ViewFormat());
                    MarketArray[i] = marketList.get(index);
                    index++;
                } else break;
            }
            itemStacks[45] = page > 1 ? PreviousPageItem : ShopFlame;
            itemStacks[46] = ItemFlame(-100);
            itemStacks[47] = ItemFlame(-10);
            itemStacks[48] = ItemFlame(-1);
            itemStacks[50] = ItemFlame(1);
            itemStacks[51] = ItemFlame(10);
            itemStacks[52] = ItemFlame(100);
            itemStacks[53] = page < Math.ceil(marketList.size()/45f) ? NextPageItem : ShopFlame;
            itemStacks[49] = ItemFlameAmount(MarketPrefix, BuyAmount);
            int i = 0;
            for (ItemStack itemStack : itemStacks) {
                player.getOpenInventory().getTopInventory().setItem(i, itemStack);
                i++;
            }
            playSound(player, SoundList.Tick);
        }
    }

    public void MarketMenuClick(InventoryView view, ItemStack currentItem, int Slot) {
        if (equalInv(view, MarketMenuDisplay)) {
            if (Slot < 45) {
                if (currentItem != null) {
                    if (MarketArray[Slot] != null) {
                        MarketContainer market = MarketContainer.getMarket(MarketArray[Slot].marketData.Owner);
                        int index = MarketArray[Slot].index;
                        MarketData marketData = market.marketData.get(index);
                        if (!MarketArray[Slot].marketData.uuid.toString().equals(marketData.uuid.toString())) {
                            Function.sendMessage(player, "§aすでに§e購入§aされているか§e出品§aが取り消されました", SoundList.Nope);
                            return;
                        }
                        int amount = BuyAmount;
                        if (amount > marketData.itemParameterStack.Amount) amount = marketData.itemParameterStack.Amount;
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
                            playSound(player, SoundList.LevelUp);
                        } else {
                            player.sendMessage("§eメル§aが足りません");
                            playSound(player, SoundList.Nope);
                        }
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
                    playSound(player, SoundList.Click);
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
        return Math.toIntExact(market.marketData.timeStamp - market2.marketData.timeStamp);
    }
}