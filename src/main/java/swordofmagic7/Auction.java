package swordofmagic7;

import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.TextView.TextView;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.PlayerData.playerData;
import static net.somrpg.swordofmagic7.SomCore.instance;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Auction {
    public static ItemParameterStack stack;
    public static PlayerData Owner;
    public static PlayerData Better;
    public static int time;
    public static int Mel;
    public static boolean Auctioning = false;

    public static void auctionCommand(PlayerData playerData, String[] args) {
        Player player = playerData.player;
        if (!player.hasPermission("som7.auction.ban")) {
            if (args.length >= 2) {
                try {
                    String type = args[0];
                    int index = Integer.parseInt(args[1]);
                    if (index > -1 && type.equalsIgnoreCase("sell") && playerData.ItemInventory.getList().size() > index) {
                        if (!Auctioning) {
                            stack = playerData.ItemInventory.getItemParameterStack(index).clone();
                            if (args.length >= 3) {
                                stack.Amount = Integer.parseInt(args[2]);
                            }
                            if (!playerData.ItemInventory.hasItemParameter(stack) || stack.Amount < 1) {
                                Function.sendMessage(player, "§e所持数§a以上は§b出品§aできません", SoundList.Nope);
                                return;
                            }
                            if (args.length >= 4) {
                                Mel = Integer.parseInt(args[3]);
                            } else Mel = 1;
                            if (Mel < 1) Mel = 1;
                            int StartMel = Mel;
                            int reqMel = (int) Math.ceil(Mel * 0.01);
                            if (playerData.Mel < reqMel) {
                                Function.sendMessage(player, "§e出品手数料§aが足りません §e[" + reqMel + "]", SoundList.Nope);
                                return;
                            } else {
                                Function.sendMessage(player, "§c[出品手数料]§e" + reqMel + "メル");
                            }
                            Owner = playerData;
                            Auctioning = true;
                            MultiThread.TaskRun(() -> {
                                TextView text = new TextView(Owner.getNick() + "§aさんが");
                                text.addView(stack.itemParameter.getTextView(stack.Amount, Owner.ViewFormat()));
                                text.addText("§aを§eオークション§aに§e" + Mel + "メル§aから§b出品§aしました");
                                Client.sendDisplay(Owner.player, text);
                                time = 30;
                                String error = null;
                                while (0 < time && instance.isEnabled()) {
                                    List<String> list = new ArrayList<>();
                                    list.add(Function.decoText("オークション"));
                                    list.add(Function.decoLore("§b§l出品者") + Owner.getNick());
                                    list.add(Function.decoLore("§b§l出品物") + "§e§l" + stack.itemParameter.Display + "§a§lx" + stack.Amount);
                                    if (Better != null) {
                                        list.add(Function.decoLore("§b§l入札額") + Mel + "メル");
                                        list.add(Function.decoLore("§b§l入札者") + Better.getNick());
                                    } else {
                                        list.add(Function.decoLore("§b§l入札額") + "§7§l未入札");
                                        list.add(Function.decoLore("§b§l入札者") + "§7§l未入札");
                                    }
                                    list.add(Function.decoLore("§b§l残り時間") + time + "秒");
                                    for (Player loopPlayer : PlayerList.PlayerList) {
                                        if (loopPlayer.isOnline()) {
                                            playerData(loopPlayer).ViewBar.setSideBar("Auction", list);
                                        }
                                    }
                                    time--;
                                    MultiThread.sleepTick(20);
                                    if (!Owner.ItemInventory.hasItemParameter(stack)) {
                                        error = "§e出品者§aが§e出品物§aを§c紛失§aしたため§eオークション§aを終了します";
                                        break;
                                    } else if (!Owner.player.isOnline()) {
                                        error = "§e出品者§aが§c失踪§aしたため§eオークション§aを終了します";
                                        break;
                                    } else if (Better != null && Better.Mel < Mel) {
                                        Better = null;
                                        Mel = StartMel;
                                        Function.BroadCast("§e入札者§aの§eメル残高§a§e入札額§a下回ったため§eオークション§aの§b入札§aを取り消します", SoundList.Tick);
                                    } else if (Better != null && !Better.player.isOnline()) {
                                        Better = null;
                                        Mel = StartMel;
                                        Function.BroadCast("§e入札者§aの§c失踪§aしたため§eオークション§aの§b入札§aを取り消します", SoundList.Tick);
                                    }
                                }
                                if (error != null) {
                                    Function.BroadCast(error, SoundList.Tick);
                                } else if (Better != null) {
                                    int reqMel2 = (int) Math.ceil(Mel * 0.05);
                                    text = new TextView(Better.getNick() + "§aさんが");
                                    text.addView(stack.itemParameter.getTextView(stack.Amount, Owner.ViewFormat()));
                                    text.addText("§aを§e" + Mel + "メル§aで§c落札§aしました");
                                    Client.sendDisplay(Better.player, text);
                                    Better.ItemInventory.addItemParameter(stack);
                                    Owner.ItemInventory.removeItemParameter(stack);
                                    Better.Mel -= Mel;
                                    Owner.Mel += (Mel-reqMel2);
                                    Function.sendMessage(player, "§c[取引手数料]§e" + reqMel2 + "メル");
                                    Better.viewUpdate();
                                    Owner.viewUpdate();
                                } else {
                                    Function.BroadCast("§e入札者§aが現れなかったため§eオークション§aが§c終了§aしました", SoundList.Tick);
                                }
                                for (Player loopPlayer : PlayerList.PlayerList) {
                                    if (loopPlayer.isOnline()) {
                                        playerData(loopPlayer).ViewBar.resetSideBar("Auction");
                                    }
                                }
                                Owner = null;
                                Better = null;
                                Auctioning = false;
                            }, "Auction");
                        } else {
                            Function.sendMessage(player, "§aすでに§eオークション§aが開催されています", SoundList.Nope);
                        }
                        return;
                    } else if (type.equalsIgnoreCase("bet")) {
                        if (Function.CheckBlockPlayer(playerData, Owner)) return;
                        if (Auctioning) {
                            if (playerData != Owner) {
                                int reqMel = (int) Math.ceil(Mel * 1.05f);
                                if (reqMel <= index) {
                                    if (playerData.Mel >= index) {
                                        Better = playerData;
                                        Mel = index;
                                        Function.BroadCast(Better.getNick() + "§aさんが§e" + index + "メル§aで§b入札§aしました", SoundList.Tick);
                                        if (time < 10) time = 10;
                                    } else {
                                        Function.sendMessage(player, "§eメル§aが足りません", SoundList.Nope);
                                    }
                                } else {
                                    Function.sendMessage(player, "§e" + reqMel + "メル§a以上でないと§b入札§a出来ません", SoundList.Nope);
                                }
                            } else {
                                player.sendMessage("§a自身の§eオークション§aには§e入札§a出来ません");
                                playSound(player, SoundList.Nope);
                            }
                        } else {
                            player.sendMessage("§eオークション§aが開催されていません");
                            playSound(player, SoundList.Nope);
                        }
                        return;
                    }
                } catch (Exception ignore) {
                }
            }
            player.sendMessage(Function.decoLore("/auction sell <SlotID> [<個数>] [<開始金額>]"));
            player.sendMessage(Function.decoLore("/auction bet <メル>"));
        } else {
            player.sendMessage("§cあなたはオークションの利用が制限されています");
            playSound(player, SoundList.Nope);
        }
    }
}
