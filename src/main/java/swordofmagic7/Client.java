package swordofmagic7;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.TextView.TextView;

import java.io.*;
import java.net.Socket;

import static swordofmagic7.Data.DataBase.DataBasePath;
import static swordofmagic7.Function.Log;
import static swordofmagic7.System.plugin;

public class Client {

    public static String Host;
    public static Socket socket;
    public static DataInputStream in;
    public static DataOutputStream out;
    public static boolean isConnection = false;

    public static void connect() {
        MultiThread.TaskRun(() -> {
            try {
                socket = new Socket(Host, 24456);
                isConnection = true;
                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                Log("通信サーバーへ接続しました -> " + socket.getLocalAddress());
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

    public static void BroadCast(TextView textView) {
        send("BroadCast," + textView.toString());
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
        if (data[0].equalsIgnoreCase("BroadCast")) {
            TextComponent finalText = new TextComponent();
            TextComponent text = new TextComponent();
            TextComponent hover = new TextComponent();
            SoundList sound = null;
            boolean isNatural = false;
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
                } else if (split[0].equalsIgnoreCase("Sound")) {
                    sound = SoundList.valueOf(split[1]);
                } else if (split[0].equalsIgnoreCase("isNatural")) {
                    isNatural = true;
                }
            }
            finalText.addExtra(text);
            Function.BroadCast(finalText, sound, isNatural);
        }  else if (data[0].equalsIgnoreCase("resultPlayerData")) {
            try {
                File file = new File(DataBasePath, "PlayerData/" + data[1] + ".yml");
                String[] fileData = data[2].split("<newLine>");
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                for (String str : fileData) {
                    writer.write(str);
                    writer.newLine();
                    Log(str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (!data[0].equalsIgnoreCase("Check")) {
            Log("無効なパケット -> " + packet);
        }
    }
}