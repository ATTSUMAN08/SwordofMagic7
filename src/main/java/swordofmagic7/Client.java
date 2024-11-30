package swordofmagic7;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.TextView.TextView;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

import static swordofmagic7.Data.DataBase.ServerId;
import static swordofmagic7.Function.Log;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.SomCore.plugin;

public class Client {
    private static final int BufferSize = 1048576;

    public static Socket socket;
    public static DataInputStream in;
    public static DataOutputStream out;
    public static boolean isConnection = false;

    /*public static void connect() {
        MultiThread.TaskRun(() -> {
            try {
                socket = new Socket(Host, 24456);
                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                Log("通信サーバーへ接続しました -> " + socket.getLocalAddress());
                isConnection = true;
                String packet;
                while (true) {
                    packet = in.readUTF();
                    if (socket.isClosed() || !plugin.isEnabled()) {
                        Log("セッションを切断します");
                        break;
                    }
                    if (packet.equalsIgnoreCase("close")) {
                        Log("セッションクローズが呼ばれました");
                        break;
                    }
                    Trigger(packet);
                    MultiThread.sleepMillis(10);
                }
                in.close();
                out.close();
                socket.close();
                Log("セッションを終了しました");
            } catch (IOException e) {
                Log("通信エラーが発生しました[0]");
                if (isConnection) {
                    Log("通信サーバーから切断されました");
                    isConnection = false;
                }
                MultiThread.sleepTick(100);
                connect();
            }
        }, "Client");
    }*/

    public static void sendBroadCast(TextView textView) {
        send("BroadCast," + textView.toString());
    }

    public static void sendPlayerChat(Player player, TextView textView) {
        send("Chat," + new TextView()
                .addView(textView)
                .setSender(Function.unColored(player.getDisplayName()))
                .setUUID(player.getUniqueId())
                .setSender(player.getName())
                .setDisplay(player.getDisplayName())
                .setMute(!player.hasPermission("snc.chat"))
                .setFrom(ServerId)
                .toString());
    }

    public static void sendDisplay(Player player, TextView textView) {
        send("Display," + new TextView()
                .addView(textView)
                .setSender(Function.unColored(player.getDisplayName()))
                .setUUID(player.getUniqueId())
                .setSender(player.getName())
                .setDisplay(player.getDisplayName())
                .setMute(!player.hasPermission("snc.chat"))
                .setFrom(ServerId)
                .toString());
    }

    public static void send(String str) {
        try {
            out.writeUTF(str);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            Log("通信サーバーに接続されていません");
        }
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
                    if (player.isOnline()) {
                        if (isMute) {
                            if (player.isOp()) {
                                TextComponent textMuted = Component.empty();
                                textMuted = textMuted.append(Component.text("§4[M]"));
                                textMuted = textMuted.append(text);
                                sendMessage(player, textMuted);
                            } else if (player.getUniqueId().toString().equals(uuid)) {
                                sendMessage(player, text);
                            }
                        } else {
                            PlayerData playerData = PlayerData.playerData(player);
                            if (uuid == null || !playerData.BlockListAtString().contains(uuid)) sendMessage(player, text);
                        }
                    }
                }
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