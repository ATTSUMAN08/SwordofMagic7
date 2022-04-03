package swordofmagic7.Pet;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Function;
import swordofmagic7.Sound.SoundList;

import java.util.Arrays;

import static swordofmagic7.Data.DataBase.AirItem;
import static swordofmagic7.Data.DataBase.AnvilUISlot;
import static swordofmagic7.Function.decoAnvil;
import static swordofmagic7.Function.equalInv;
import static swordofmagic7.Sound.CustomSound.playSound;

public class PetEvolution {

    private final Player player;
    private final PlayerData playerData;
    private static final String PetEvolutionDisplay = "§lペット進化";

    public PetEvolution(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    public void PetEvolutionView() {
        playerData.setView(ViewInventoryType.PetInventory, false);
        Inventory inv = decoAnvil(PetEvolutionDisplay);
        player.openInventory(inv);
    }

    private final PetParameter[] Cache = new PetParameter[3];
    public void PetEvolutionClick(InventoryView view, Inventory ClickInventory, int index, int Slot) {
        if (equalInv(view, PetEvolutionDisplay)) {
            if (view.getTopInventory() == ClickInventory) {
                if (Slot == AnvilUISlot[2]) {
                    if (Cache[2] != null) {
                        playerData.PetInventory.addPetParameter(Cache[2]);
                        player.sendMessage("§e[" + Cache[2].petData.Display + "]§aの§e最大レベル§aが§eLv" + Cache[2].MaxLevel + "§aに進化しました");
                        playSound(player, SoundList.LevelUp);
                        Arrays.fill(Cache, null);
                    }
                } else {
                    if (Slot == AnvilUISlot[0] && Cache[0] != null) {
                        playerData.PetInventory.addPetParameter(Cache[0]);
                        Cache[0] = null;
                        playSound(player, SoundList.Click);
                        if (Cache[1] != null) {
                            playerData.PetInventory.addPetParameter(Cache[1]);
                            Cache[1] = null;
                        }
                    } else if (Slot == AnvilUISlot[1] && Cache[1] != null) {
                        playerData.PetInventory.addPetParameter(Cache[1]);
                        Cache[1] = null;
                        playSound(player, SoundList.Click);
                    }
                }
            } else if (index > -1) {
                PetParameter petParameter = playerData.PetInventory.getPetParameter(index);
                if (Cache[0] == null || Cache[1] == null) {
                    if (Cache[0] == null) {
                        if (petParameter.MaxLevel < PlayerData.MaxLevel) {
                            Cache[0] = petParameter;
                            playerData.PetInventory.removePetParameter(index);
                        } else {
                            Function.sendMessage(player, "§aこれ以上§c最大レベル§aを上げれません", SoundList.Nope);
                        }
                    } else {
                        if (Cache[0].petData.Id.equals(petParameter.petData.Id)) {
                            if (Cache[0].MaxLevel >= petParameter.MaxLevel) {
                                Cache[1] = petParameter;
                                playerData.PetInventory.removePetParameter(index);
                            } else {
                                Function.sendMessage(player, "§e素体ペット§aより§e最大レベル§a高い§eペット§aは素材に出来ません", SoundList.Nope);
                            }
                        } else {
                            Function.sendMessage(player, "§e同種ペット§aのみ§e素材に出来ます", SoundList.Nope);
                        }
                    }
                    playSound(player, SoundList.Click);
                }
            }
            String format = playerData.ViewFormat();
            Inventory inv = player.getOpenInventory().getTopInventory();
            for (int i = 0; i < 2; i++) {
                if (Cache[i] != null) {
                    inv.setItem(AnvilUISlot[i], Cache[i].viewPet(format));
                } else {
                    inv.setItem(AnvilUISlot[i], AirItem);
                }
            }
            if (Cache[0] != null && Cache[1] != null) {
                Cache[2] = Cache[0].clone();
                Cache[2].MaxLevel = Math.min(Cache[2].MaxLevel + 5,  PlayerData.MaxLevel);
                inv.setItem(AnvilUISlot[2], Cache[2].viewPet(format));
            } else {
                inv.setItem(AnvilUISlot[2], AirItem);
            }
        }
    }

    public void PetEvolutionClose(InventoryView view) {
        player.setItemOnCursor(AirItem);
        if (equalInv(view, PetEvolutionDisplay)) {
            for (int i = 0; i < 2; i++) {
                if (Cache[i] != null) {
                    playerData.PetInventory.addPetParameter(Cache[i]);
                    Cache[i] = null;
                }
            }
        }
    }
}
