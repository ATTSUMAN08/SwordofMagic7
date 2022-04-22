package swordofmagic7.Party;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Function;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.*;
import static swordofmagic7.Sound.CustomSound.playSound;

public class PartyManager {
    public static void save() {

    }

    public static HashMap<String, PartyData> PartyList = new HashMap<>();
    public static HashMap<Player, PartyData> PartyInvites = new HashMap<>();
    public static HashMap<String, String> PartyRejoin = new HashMap<>();

    public static void rejoinCheck(Player player) {
        String uuid = player.getUniqueId().toString();
        if (PartyRejoin.containsKey(uuid)) {
            String party = PartyRejoin.get(uuid);
            if (PartyList.containsKey(party)) {
                PartyList.get(party).Join(player);
            }
        }
    }

    public static void partyCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("create")) {
                if (playerData.Party == null) {
                    String Name;
                    if (args.length == 2) {
                        Name = args[1];
                    } else {
                        Name = playerData.Nick + "のパーティ";
                    }
                    if (!PartyList.containsKey(Name)) {
                        playerData.Party = new PartyData(player, Name, Name);
                        PartyList.put(Name, playerData.Party);
                    } else {
                        player.sendMessage("§aすでに§e[" + Name + "]§aは使用されています");
                        playSound(player, SoundList.Nope);
                    }
                } else {
                    player.sendMessage("§e[" + playerData.Party.Display + "]§aに参加しています");
                    playSound(player, SoundList.Nope);
                }
                return;
            } else if (args[0].equalsIgnoreCase("join") && args.length == 2) {
                String Name = args[1];
                if (PartyList.containsKey(Name)) {
                    PartyData partyData = PartyList.get(Name);
                    if (partyData.Public) {
                        partyData.Join(player);
                    } else {
                        player.sendMessage("§e[" + Name + "]§aは§c[非公開]§aです");
                        playSound(player, SoundList.Nope);
                    }
                } else {
                    player.sendMessage("§a存在しない§eパーティ§aです");
                    playSound(player, SoundList.Nope);
                }
                return;
            } else if (args[0].equalsIgnoreCase("list")) {
                player.sendMessage(decoText("§e公開パーティリスト"));
                boolean none = true;
                for (PartyData partyData : PartyList.values()) {
                    if (partyData.Public) {
                        player.sendMessage("§7・§e" + partyData.Display + "§7: §a" + partyData.Members.size() + "人");
                        none = false;
                    }
                }
                if (none) player.sendMessage("§7・§c公開中のパーティなし");
                return;
            } else if (args[0].equalsIgnoreCase("chat")) {
                if (playerData.Party != null) {
                    if (args.length >= 2) {
                        playerData.Party.chat(playerData, args[1]);
                    } else {
                        playerData.isPTChat = ! playerData.isPTChat;
                        sendMessage(player, "§eチャットモード§aを§e[" + (playerData.isPTChat ? "パーティ" : "全体") + "]§aにしました");
                    }
                } else {
                    Function.sendMessage(player, "§eパーティ§aに参加していません", SoundList.Tick);
                }
                return;
            }
            if (PartyInvites.containsKey(player)) {
                if (args[0].equalsIgnoreCase("accept")) {
                    PartyInvites.get(player).Join(player);
                    PartyInvites.remove(player);
                    return;
                } else if (args[0].equalsIgnoreCase("decline")) {
                    PartyInvites.get(player).Message(playerData.getNick() + "§aさんが§e招待§aを§c拒否§aしました");
                    PartyInvites.remove(player);
                    playSound(player, SoundList.Tick);
                    return;
                }
            }
            if (playerData.Party != null) {
                if (args[0].equalsIgnoreCase("leave")) {
                    playerData.Party.Quit(player);
                    return;
                } else if (args[0].equalsIgnoreCase("info")) {
                    for (String msg : playerData.Party.view()) {
                        player.sendMessage(msg);
                    }
                    return;
                } else if (playerData.Party.Leader == player) {
                    if (args[0].equalsIgnoreCase("invite") && args.length <= 2) {
                        for (int i = 1; i < args.length; i++) {
                            Player invite = Bukkit.getPlayer(args[i]);
                            if (invite != null) {
                                if (!playerData.Party.Members.contains(invite)) {
                                    playerData.Party.Invite(invite);
                                } else {
                                    player.sendMessage("§aすでに§e[" + playerData.Party.Display + "]§aに参加しています");
                                    playSound(player, SoundList.Nope);
                                }
                            } else {
                                player.sendMessage("§a存在しない§eプレイヤー§aです");
                                playSound(player, SoundList.Nope);
                            }
                        }
                        return;
                    } else if (args[0].equalsIgnoreCase("promote") && args.length == 2) {
                        Player promote = Bukkit.getPlayer(args[1]);
                        if (promote != null && playerData.Party.Members.contains(promote)) {
                            playerData.Party.Promote(promote);
                        } else {
                            player.sendMessage("§e[" + playerData.Party.Display + "]§aに参加していない§eプレイヤー§aです");
                            playSound(player, SoundList.Nope);
                        }
                        return;
                    } else if (args[0].equalsIgnoreCase("kick") && args.length == 2) {
                        Player kick = Bukkit.getPlayer(args[1]);
                        if (kick != null && playerData.Party.Members.contains(kick)) {
                            playerData.Party.Message(playerData(kick).getNick() + "§aさんが§c追放§aされました");
                            playerData.Party.Quit(kick);
                        } else {
                            player.sendMessage("§e[" + playerData.Party.Display + "]§aに参加していない§eプレイヤー§aです");
                            playSound(player, SoundList.Nope);
                        }
                        return;
                    } else if (args[0].equalsIgnoreCase("lore") && args.length == 2) {
                        playerData.Party.setLore(args[1]);
                        player.sendMessage("§e説明文§aを更新しました");
                        playSound(player, SoundList.Click);
                        return;
                    } else if (args[0].equalsIgnoreCase("toggle")) {
                        PartyData party = playerData.Party;
                        party.Public = !party.Public;
                        if (party.Public) party.Message("§e[" + party.Display + "]§aを§b公開§aしました");
                        else party.Message("§e[" + party.Display + "]§aを§c非公開§aにしました");
                        return;
                    } else {
                        boolean check = false;
                        for (String str : args) {
                            if (Bukkit.getPlayer(str) != null) {
                                player.performCommand("party invite " + str);
                                check = true;
                            }
                        }
                        if (check) return;
                    }
                }
            }
        }
        player.sendMessage(decoLore("/party create <Name>") + "パーティを作成します");
        player.sendMessage(decoLore("/party join <Party>") + "公開パーティに参加します");
        player.sendMessage(decoLore("/party list") + "公開パーティ一覧を表示します");
        if (PartyInvites.containsKey(player)) {
            player.sendMessage(decoLore("/party accept") + "パーティ招待を承認します");
            player.sendMessage(decoLore("/party decline") + "パーティ招待を拒否します");
        }
        if (playerData.Party != null) {
            player.sendMessage(decoLore("/party leave") + "パーティから脱退します");
            player.sendMessage(decoLore("/party info") + "パーティの情報を表示します");
            if (playerData.Party.Leader == player) {
                player.sendMessage(decoLore("/party <Player> [<Player>] etc...") + "複数のプレイヤーをパーティに招待します");
                player.sendMessage(decoLore("/party invite <Player>") + "プレイヤーをパーティに招待します");
                player.sendMessage(decoLore("/party promote <Player>") + "リーダー権を譲渡します");
                player.sendMessage(decoLore("/party kick <Player>") + "プレイヤーをパーティから追放します");
                player.sendMessage(decoLore("/party lore <Text>") + "パーティの説明文を設定ます");
                player.sendMessage(decoLore("/party toggle") + "パーティの公開設定を切り替えます");
            }
        }
    }
}
