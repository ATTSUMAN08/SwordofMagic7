package swordofmagic7.Trade;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;
import java.util.UUID;

import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.NonMel;
import static swordofmagic7.Sound.CustomSound.playSound;

public class TradeManager {
    public static HashMap<UUID, TradeData> TradeList = new HashMap<>();
    public static HashMap<Player, TradeData> TradeRequest = new HashMap<>();

    public static final String nonTradeMessage = "§cトレード不可§aです";
    public static boolean nonTrade(Player player, ItemParameter param) {
        if (param.isNonTrade) {
            sendMessage(player, "§e[" + param.Display + "]§aは" + nonTradeMessage, SoundList.NOPE);
            return true;
        } else return false;
    }
    public static boolean nonTrade(Player player, RuneParameter param) {
        if (param.isNonTrade) {
            sendMessage(player, "§e[" + param.Display + "]§aは" + nonTradeMessage, SoundList.NOPE);
            return true;
        } else return false;
    }
    public static boolean nonTrade(Player player, PetParameter param) {
        if (param.petData.isNonTrade) {
            sendMessage(player, "§e[" + param.petData.Display + "]§aは" + nonTradeMessage, SoundList.NOPE);
            return true;
        } else return false;
    }

    public static void tradeCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length > 0) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null && target.isOnline() && target != player) {
                PlayerData targetData = PlayerData.playerData(target);
                if (CheckBlockPlayer(playerData, targetData)) return;
                if (args.length == 1) {
                    TradeData tradeData = new TradeData(player, target);
                    tradeData.requestTrade();
                    TradeRequest.put(target, tradeData);
                    return;
                } else if (args.length >= 3) {
                    try {
                        int index = Integer.parseInt(args[2]);
                        if (args[1].equalsIgnoreCase("sendItem") || args[1].equalsIgnoreCase("sI")) {
                            if (playerData.ItemInventory.getList().size() > index) {
                                ItemParameterStack stack = playerData.ItemInventory.getItemParameterStack(index);
                                if (nonTrade(player, stack.itemParameter)) return;
                                int Amount = args.length == 4 ? Integer.parseInt(args[3]) : stack.Amount;
                                if (Amount > 0 ) {
                                    ItemParameter item = stack.itemParameter;
                                    if (stack.Amount >= Amount) {
                                        targetData.ItemInventory.addItemParameter(item, Amount);
                                        playerData.ItemInventory.removeItemParameter(item, Amount);
                                        player.sendMessage(targetData.getNick() + "§aさんに§e[" + item.Display + "§ax" + Amount + "§e]§aを送りました");
                                        target.sendMessage(playerData.getNick() + "§aさんから§e[" + item.Display + "§ax" + Amount + "§e]§aが送られてきました");
                                        playerData.viewUpdate();
                                        targetData.viewUpdate();
                                        playSound(player, SoundList.TICK);
                                        playSound(target, SoundList.TICK);
                                    } else {
                                        player.sendMessage("§c所持数§aが足りません");
                                        playSound(player, SoundList.NOPE);
                                    }
                                } else {
                                    player.sendMessage("§c不正§aな§eスロット§aです");
                                    playSound(player, SoundList.NOPE);
                                }
                            } else {
                                player.sendMessage("§c不正§aな§eスロット§aです");
                                playSound(player, SoundList.NOPE);
                            }
                            return;
                        } else if (args[1].equalsIgnoreCase("sendRune") || args[1].equalsIgnoreCase("sR")) {
                            if (playerData.RuneInventory.getList().size() > index) {
                                int toIndex = args.length == 4 ? Integer.parseInt(args[3]) : index;
                                for (int i = index; i <= toIndex; i++) {
                                    RuneParameter rune = playerData.RuneInventory.getRuneParameter(index);
                                    if (nonTrade(player, rune)) return;
                                    targetData.RuneInventory.addRuneParameter(rune);
                                    playerData.RuneInventory.removeRuneParameter(index);
                                    player.sendMessage(targetData.getNick() + "§aさんに§e[" + rune.Display + "§e]§aを送りました");
                                    target.sendMessage(playerData.getNick() + "§aさんから§e[" + rune.Display + "§e]§aが送られてきました");
                                }
                                playerData.viewUpdate();
                                targetData.viewUpdate();
                                playSound(player, SoundList.TICK);
                                playSound(target, SoundList.TICK);
                            } else {
                                player.sendMessage("§c不正§aな§eスロット§aです");
                                playSound(player, SoundList.NOPE);
                            }
                            return;
                        } else if (args[1].equalsIgnoreCase("sendPet") || args[1].equalsIgnoreCase("sP")) {
                            if (playerData.PetInventory.getList().size() > index) {
                                PetParameter pet = playerData.PetInventory.getPetParameter(index);
                                if (nonTrade(player, pet)) return;
                                if (!pet.Summoned) {
                                    pet.player = target;
                                    pet.playerData = targetData;
                                    targetData.PetInventory.addPetParameter(pet);
                                    playerData.PetInventory.removePetParameter(index);
                                    player.sendMessage(targetData.getNick() + "§aさんに§e[" + pet.petData.Display + "§e]§aを送りました");
                                    target.sendMessage(playerData.getNick() + "§aさんから§e[" + pet.petData.Display + "§e]§aが送られてきました");
                                    playerData.viewUpdate();
                                    targetData.viewUpdate();
                                    playSound(player, SoundList.TICK);
                                    playSound(target, SoundList.TICK);
                                } else {
                                    player.sendMessage("§e[ペット]§aが召喚されています");
                                    playSound(player, SoundList.NOPE);
                                }
                            } else {
                                player.sendMessage("§c不正§aな§eスロット§aです");
                                playSound(player, SoundList.NOPE);
                            }
                            return;
                        } else if (index > 0) {
                            if (args[1].equalsIgnoreCase("sendMel") || args[1].equalsIgnoreCase("sM")) {
                                if (playerData.Mel >= index) {
                                    targetData.Mel += index;
                                    playerData.Mel -= index;
                                    player.sendMessage(targetData.getNick() + "§aさんに§e[" + index + "メル]§aを送りました");
                                    target.sendMessage(playerData.getNick() + "§aさんから§e[" + index + "メル]§aが送られてきました");
                                    playSound(player, SoundList.TICK);
                                    playSound(target, SoundList.TICK);
                                } else {
                                    player.sendMessage(NonMel);
                                    playSound(player, SoundList.NOPE);
                                }
                            }
                            return;
                        }
                    } catch (Exception ignored) {}
                }
            } else if (TradeRequest.containsKey(player)) {
                if (args[0].equalsIgnoreCase("accept")) {
                    TradeRequest.get(player).requestAccept();
                    return;
                } else if (args[0].equalsIgnoreCase("decline")) {
                    TradeRequest.get(player).requestDecline();
                    return;
                }
            }
        }
        player.sendMessage(decoLore("/trade <Player> sendItem <SlotID> [<Amount>]") + "アイテムを送ります");
        player.sendMessage(decoLore("/trade <Player> sendRune <SlotID> [<toSlotID>]") + "ルーンを送ります");
        player.sendMessage(decoLore("/trade <Player> sendPet <SlotID>") + "ペットを送ります");
        player.sendMessage(decoLore("/trade <Player> sendMel <Mel>") + "メルを送ります");
        //player.sendMessage(decoLore("/trade <Player>") + "トレードを申請します");
        if (TradeRequest.containsKey(player)) {
            player.sendMessage(decoLore("/trade accept") + "トレード申請を承認します");
            player.sendMessage(decoLore("/trade decline") + "トレード申請を拒否します");
        }
    }

    public void TradeClick(Inventory inv) {

    }
}
