package swordofmagic7;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.TextView.TextView;

import java.io.*;
import java.net.Socket;

import static swordofmagic7.Data.DataBase.ServerId;
import static swordofmagic7.Function.Log;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.SomCore.plugin;

public class Client {

    private static final int BufferSize = 1048576;

    public static String Host;
    public static Socket socket;
    public static DataInputStream in;
    public static DataOutputStream out;
    public static boolean isConnection = false;

    public static void connect() {
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
    }

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
            Log("通信サーバーの接続されていません");
        }
    }

    private static final TextComponent newLine = new TextComponent(ComponentSerializer.parse("{text: \"\n\"}"));
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
                TextComponent text = new TextComponent();
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
                if (data[0].equals("Chat")) text.addExtra("§b[" + from + "] §r" + display + "§a: §r");
                if (data[0].equals("Display")) text.addExtra("§b[" + from + "] §r");
                text.addExtra(textComponentFromPacket(data));
                for (Player player : PlayerList.get()) {
                    if (player.isOnline()) {
                        if (isMute) {
                            if (player.isOp()) {
                                TextComponent textMuted = new TextComponent();
                                textMuted.addExtra("§4[M]");
                                textMuted.addExtra(text);
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

    public static TextComponent textComponentFromPacket(String[] data) {
        TextComponent finalText = new TextComponent();
        TextComponent text = new TextComponent();
        TextComponent hover = new TextComponent();
        for (int i = 1; i < data.length; i++) {
            String[] split = data[i].split(":", 2);
            if (split[0].equalsIgnoreCase("Reset")) {
                finalText.addExtra(text);
                text = new TextComponent();
            } else if (split[0].equalsIgnoreCase("Text")) {
                text.addExtra(split[1]);
            } else if (split[0].equalsIgnoreCase("Hover")) {
                boolean first = true;
                for (String str : split[1].split("\n")) {
                    if (!first) hover.addExtra(newLine);
                    hover.addExtra(str);
                    first = false;
                }
                text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
                hover = new TextComponent();
            }
        }
        finalText.addExtra(text);
        return finalText;
    }
}