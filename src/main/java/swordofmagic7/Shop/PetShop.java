package swordofmagic7.Shop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Item.ItemStackData;
import swordofmagic7.Pet.PetData;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.NonMel;
import static swordofmagic7.Sound.CustomSound.playSound;

public class PetShop {

    private static final String PetShopDisplay = "§lペットショップ";
    public static final String PetSyntheticDisplay = "§lペット配合";
    public static final String PetEvolutionDisplay = "§lペット進化";
    public static final String PetSellDisplay = "§lペット売却";
    private static final ItemStack PetShopFreeWolf = new ItemStackData(Material.WOLF_SPAWN_EGG, decoText("オースオオカミ"), "§a§l100メルの配布ペットです").view();
    private static final ItemStack PetSynthetic = new ItemStackData(Material.HEART_OF_THE_SEA, decoText("ペット配合"), "§a§l同種のペットを配合して成長率を上げます\n§a§l成長率は合計の70%の値になります\n§a§l成長率の上限は200%です").view();
    private static final ItemStack PetEvolution = new ItemStackData(Material.END_CRYSTAL, decoText("ペット進化"), "§a§l同種のペットを配合して\n§a§l最大レベルを上げます\n§a§l上限は§e§lLv" + PlayerData.MaxLevel + "§a§lです").view();
    private static final ItemStack PetSellItem = new ItemStackData(Material.GOLD_NUGGET, decoText("ペット売却"), "§a§lペットショップにペットを売ります").view();

    private final Player player;
    private final PlayerData playerData;


    public PetShop(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    public void PetShopOpen() {
        Inventory inv = decoInv(PetShopDisplay, 1);
        inv.setItem(0, PetShopFreeWolf);
        inv.setItem(1, PetSynthetic);
        inv.setItem(2, PetEvolution);
        inv.setItem(8, PetSellItem);
        player.openInventory(inv);
    }

    private final List<PetParameter> PetSell = new ArrayList<>();
    public void PetSellView() {
        player.openInventory(PetSellInv());
    }

    public Inventory PetSellInv() {
        Inventory inv = decoInv(PetSellDisplay, 5);
        playerData.setView(ViewInventoryType.PetInventory, false);
        int slot = 0;
        for (PetParameter pet : PetSell) {
            if (pet != null) {
                inv.setItem(slot, pet.viewPet(playerData.ViewFormat()));
                slot++;
            }
        }
        return inv;
    }

    public void PetSyntheticOpen() {
        Inventory inv = decoAnvil(PetSyntheticDisplay);
        playerData.setView(ViewInventoryType.PetInventory, false);
        player.openInventory(inv);
    }

    public PetParameter[] PetSyntheticCache = new PetParameter[3];
    private final static int Mel = 100;
    public void PetShopClick(InventoryView view, Inventory ClickInventory, ItemStack currentItem, int index, int Slot) {
        if (equalInv(view, PetShopDisplay)) {
            if (equalItem(currentItem, PetShopFreeWolf)) {
                if (!playerData.PetInventory.hasPetParameter("オースオオカミ")) {
                    if (playerData.Mel >= 100) {
                        playerData.Mel -= 100;
                        PetData petData = getPetData("オースオオカミ");
                        PetParameter petParameter = new PetParameter(player, playerData, petData, 1, 30, 0, 1);
                        playerData.PetInventory.addPetParameter(petParameter);
                        player.sendMessage("§e[" + petData.Display + "]§aを受け取りました");
                        playSound(player, SoundList.LevelUp);
                    } else {
                        player.sendMessage(NonMel);
                        playSound(player, SoundList.Nope);
                    }
                } else {
                    player.sendMessage("§aすでに§eペット§aを所持しています");
                    playSound(player, SoundList.Nope);
                }
            } else if (equalItem(currentItem, PetSynthetic)) {
                PetSyntheticOpen();
                playSound(player, SoundList.MenuOpen);
            } else if (equalItem(currentItem, PetEvolution)) {
                playerData.PetEvolution.PetEvolutionView();
                playSound(player, SoundList.MenuOpen);
            } else if (equalItem(currentItem, PetSellItem)) {
                PetSellView();
                playSound(player, SoundList.MenuOpen);
            }
        } else if (equalInv(view, PetSellDisplay) && playerData.ViewInventory.isPet()) {
            if (view.getTopInventory() == ClickInventory) {
                if (Slot < 45) {
                    if (PetSell.size() > Slot) {
                        PetParameter pet = PetSell.get(Slot);
                        if (playerData.Mel >= Mel) {
                            playerData.Mel -= Mel;
                            playerData.PetInventory.addPetParameter(pet);
                            PetSell.remove(pet);
                            player.sendMessage("§e[" + pet.petData.Display + "§e]§aを§b買戻§aしました §c[-" + Mel + "メル]");
                            playSound(player, SoundList.LevelUp);
                        } else {
                            player.sendMessage(NonMel + " §c不足[" + (Mel-playerData.Mel) + "メル]");
                            playSound(player, SoundList.Nope);
                        }
                    }
                }
            } else if (ClickInventory == player.getInventory() && index > -1) {
                PetParameter pet = playerData.PetInventory.getPetParameter(index);
                PetSell.add(pet);
                playerData.PetInventory.removePetParameter(index);
                playerData.Mel += Mel;
                if (PetSell.size() > 45) {
                    PetSell.remove(0);
                }
                player.sendMessage("§e[" + pet.petData.Display + "§e]§aを§c売却§aしました §e[+" + Mel + "メル]");
                playSound(player, SoundList.LevelUp);
            }
            view.getTopInventory().setContents(PetSellInv().getStorageContents());
        } else if (equalInv(view, PetSyntheticDisplay)) {
            String format = playerData.ViewFormat();
            Inventory inv = view.getTopInventory();
            if (ClickInventory == view.getTopInventory()) {
                if (Slot == AnvilUISlot[0] || Slot == AnvilUISlot[1]) {
                    for (int i = 0; i < 2; i++) {
                        if (PetSyntheticCache[i] != null) {
                            playerData.PetInventory.addPetParameter(PetSyntheticCache[i]);
                            PetSyntheticCache[i] = null;
                        }
                    }
                    playSound(player, SoundList.Click);
                } else if (Slot == AnvilUISlot[2] && PetSyntheticCache[2] != null) {
                    int mel = (int) Math.round(PetSyntheticCache[2].Level*PetSyntheticCache[2].GrowthRate*5+100);
                    if (playerData.Mel >= mel) {
                        playerData.Mel -= mel;
                        playerData.PetInventory.addPetParameter(PetSyntheticCache[2]);
                        PetSyntheticCache[2] = null;
                        for (int i = 0; i < 2; i++) {
                            PetSyntheticCache[i] = null;
                        }
                        player.sendMessage("§e[ペット]§aを§b配合§aしました §c[-" + mel + "メル]");
                        playSound(player, SoundList.LevelUp);
                    } else {
                        sendMessage(player, "§eメル§aが足りません §c[" + mel + "メル]", SoundList.Nope);
                    }
                }
            } else if (index > -1) {
                PetParameter pet = playerData.PetInventory.getPetParameter(index).clone();
                if (!pet.Summoned) {
                    if (PetSyntheticCache[0] == null) {
                        PetSyntheticCache[0] = pet;
                        playerData.PetInventory.removePetParameter(index);
                        playSound(player, SoundList.Click);
                    } else if (PetSyntheticCache[1] == null) {
                        if (PetSyntheticCache[0].petData.Id.equals(pet.petData.Id)) {
                            PetSyntheticCache[1] = pet.clone();
                            playerData.PetInventory.removePetParameter(index);
                            playSound(player, SoundList.Click);
                        } else {
                            player.sendMessage("§e[同種]§aの§e[ペット]§aを選択してください");
                            playSound(player, SoundList.Nope);
                        }
                    }
                } else {
                    player.sendMessage("§e[ペット]§aを§e[ケージ]§aに戻してください");
                    playSound(player, SoundList.Nope);
                }
            }
            for (int i = 0; i < 2; i++) {
                if (PetSyntheticCache[i] != null) {
                    inv.setItem(AnvilUISlot[i], PetSyntheticCache[i].viewPet(format));
                } else {
                    inv.setItem(AnvilUISlot[i], AirItem);
                }
            }
            if (PetSyntheticCache[0] != null && PetSyntheticCache[1] != null) {
                PetSyntheticCache[2] = PetSyntheticCache[0].clone();
                PetSyntheticCache[2].Level = Math.max(PetSyntheticCache[0].Level, PetSyntheticCache[1].Level);
                PetSyntheticCache[2].updateStatus();
                PetSyntheticCache[2].GrowthRate = (PetSyntheticCache[0].GrowthRate+PetSyntheticCache[1].GrowthRate)*0.7;
                if (PetSyntheticCache[2].GrowthRate > 2) PetSyntheticCache[2].GrowthRate = 2;
                inv.setItem(AnvilUISlot[2], PetSyntheticCache[2].viewPet(format));
            } else {
                inv.setItem(AnvilUISlot[2], AirItem);
                PetSyntheticCache[2] = null;
            }
        }
    }

    public void PetSyntheticClose(InventoryView view) {
        player.setItemOnCursor(AirItem);
        if (equalInv(view, PetSyntheticDisplay)) {
            for (int i = 0; i < 2; i++) {
                if (PetSyntheticCache[i] != null) {
                    playerData.PetInventory.addPetParameter(PetSyntheticCache[i]);
                    PetSyntheticCache[i] = null;
                }
            }
        }
    }
}
