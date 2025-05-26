package swordofmagic7;

import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import me.attsuman08.abysslib.RedisManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.somrpg.swordofmagic7.SomCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.TextView.TextView;

import java.util.Objects;

import static swordofmagic7.Data.DataBase.ServerId;
import static swordofmagic7.Function.Log;
import static swordofmagic7.Function.sendMessage;

public class Client {

    public static void sendBroadCast(TextView textView) {
        send("BroadCast," + textView.toString());
    }

    public static void sendPlayerChat(Player player, TextView textView) {
        String displayName = PlainTextComponentSerializer.plainText().serialize(player.displayName());
        send("Chat," + new TextView()
                .addView(textView)
                .setSender(Function.unColored(displayName))
                .setUUID(player.getUniqueId())
                .setSender(player.getName())
                .setDisplay(displayName)
                .setMute(!player.hasPermission("som7.chat"))
                .setFrom(ServerId)
                .toString());
    }

    public static void sendDisplay(Player player, TextView textView) {
        String displayName = PlainTextComponentSerializer.plainText().serialize(player.displayName());
        send("Display," + new TextView()
                .addView(textView)
                .setSender(Function.unColored(displayName))
                .setUUID(player.getUniqueId())
                .setSender(player.getName())
                .setDisplay(displayName)
                .setMute(!player.hasPermission("som7.chat"))
                .setFrom(ServerId)
                .toString());
    }

    public static void send(String str) {
        RedisManager.Companion.getAPI().publishMessage("SNC", str);
    }

    public static void Trigger(String packet) {
        String[] data = packet.split(",");
        switch (data[0]) {
            case "BroadCast" -> {
                SoundList sound = null;
                boolean isNatural = false;
                for (int i = 1; i < data.length; i++) {
                    String[] split = data[i].split(":", 2);
                    if (split[0].equalsIgnoreCase("Sound")) {
                        sound = SoundList.valueOf(split[1]);
                    } else if (split[0].equalsIgnoreCase("isNatural")) {
                        isNatural = true;
                    }
                }
                Function.BroadCast(textComponentFromPacket(data), sound, isNatural);
            }
            case "Chat", "Display" -> {
                TextComponent text = Component.empty();
                String from = "";
                String display = "";
                String uuid = "";
                boolean isMute = false;
                for (int i = 1; i < data.length; i++) {
                    String[] split = data[i].split(":", 2);
                    if (split[0].equalsIgnoreCase("UUID")) {
                        uuid = split[1];
                    } else if (split[0].equalsIgnoreCase("Display")) {
                        display = split[1];
                    } else if (split[0].equalsIgnoreCase("From")) {
                        from = split[1];
                    } else if (split[0].equalsIgnoreCase("isMute")) {
                        isMute = true;
                    }
                }
                if (data[0].equals("Chat")) text = text.append(Component.text("§b[" + from + "] §r" + display + "§a: §r"));
                if (data[0].equals("Display")) text = text.append(Component.text("§b[" + from + "] §r"));
                text = text.append(textComponentFromPacket(data));
                for (Player player : PlayerList.get()) {
                    if (!player.isOnline()) continue;

                    if (isMute && player.isOp()) {
                        TextComponent textMuted = Component.empty();
                        textMuted = textMuted.append(Component.text("§4[M]"));
                        textMuted = textMuted.append(text);
                        sendMessage(player, textMuted);
                    } else if (isMute && player.getUniqueId().toString().equals(uuid)) {
                        sendMessage(player, text);
                    } else {
                        PlayerData playerData = PlayerData.playerData(player);
                        if (uuid == null || !playerData.BlockListAtString().contains(uuid)) sendMessage(player, text);
                    }
                }

                if (SomCore.Companion.isDevServer() && !isMute && !Objects.equals(from, "Discord")) {
                    WebhookMessage webhookMessage = new WebhookMessageBuilder()
                            .setContent(Function.unColored(PlainTextComponentSerializer.plainText().serialize(textComponentFromPacket(data))))
                            .setAllowedMentions(AllowedMentions.none())
                            .setUsername("[" + from + "] " + Function.unColored(display))
                            .setAvatarUrl("https://crafthead.net/avatar/" + uuid)
                            .build();
                    SomCore.instance.sendDiscordMessage(webhookMessage);
                }
                Bukkit.getConsoleSender().sendMessage(PlainTextComponentSerializer.plainText().serialize(text) + " [ミュート: " + isMute + "]");
            }
            case "Check" -> {}
            default -> Log("§c無効なパケット -> " + packet);
        }
    }

    public static Component textComponentFromPacket(String[] data) {
        Component finalText = Component.empty();
        Component text = Component.empty();
        Component hover = Component.empty();
        for (int i = 1; i < data.length; i++) {
            String[] split = data[i].split(":", 2);
            if (split[0].equalsIgnoreCase("Reset")) {
                finalText = finalText.append(text);
                text = Component.empty();
            } else if (split[0].equalsIgnoreCase("Text")) {
                text = text.append(Component.text(split[1]));
            } else if (split[0].equalsIgnoreCase("Hover")) {
                boolean first = true;
                for (String str : split[1].split("\n")) {
                    if (!first) hover = hover.appendNewline();
                    hover = hover.append(Component.text(str));
                    first = false;
                }
                text = text.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
                hover = Component.empty();
            }
        }
        finalText = finalText.append(text);
        return finalText;
    }
}