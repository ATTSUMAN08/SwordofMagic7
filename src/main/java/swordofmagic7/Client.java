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
import java.util.Arrays;

import static swordofmagic7.Data.DataBase.DataBasePath;
import static swordofmagic7.Data.DataBase.ServerId;
import static swordofmagic7.Function.BroadCast;
import static swordofmagic7.Function.Log;

public class Client {

    public static String Host;
    public static Socket socket;
    public static PrintWriter writer;
    public static boolean isConnection = false;

    public static void connect() {
        MultiThread.TaskRun(() -> {
            try {
                socket = new Socket(Host, 24456);
                Log("通信サーバーへ接続しました -> " + socket.getLocalAddress());
                //FileClient.start();
                isConnection = true;
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);
                String line;
                while (!socket.isClosed()) {
                    line = reader.readLine();
                    Trigger(line);
                    if (line.equalsIgnoreCase("close")) break;
                    MultiThread.sleepMillis(10);
                }
                reader.close();
                writer.close();
            } catch (IOException e) {
                if (isConnection) {
                    Log("通信サーバーから切断されました");
                    isConnection = false;
                }
                MultiThread.sleepTick(20);
                connect();
            }
        }, "Client");
    }

    public static void send(TextView textView) {
        send(textView.toString());
    }

    public static void send(String str) {
        try {
            if (socket != null && socket.isConnected()) {
                writer.println("§b[CH-" + ServerId + "]§r " + str);
            } else {
                Log("通信サーバーの接続されていません");
            }
        } catch (Exception ignore) {}
    }

    private static final TextComponent newLine = new TextComponent(ComponentSerializer.parse("{text: \"\n\"}"));
    public static void Trigger(String strData) {
        String[] data = strData.split(",");
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
                Log("split -> " + Arrays.toString(split));
            }
            finalText.addExtra(text);
            BroadCast(finalText, sound, isNatural);
        } else {
            Log("無効なパケット -> " + Arrays.toString(data));
        }
    }
}

class FileClient {

    public static FileClient fileClient;
    public static ObjectInputStream input;
    public static ObjectOutputStream output;

    public static void start() {
        MultiThread.TaskRun(() ->{
            try {
                Socket socket = new Socket(Client.Host, 24457);
                Log("ファイルサーバーへ接続しました -> " + socket.getLocalAddress());
                input = new ObjectInputStream(socket.getInputStream());
                output = new ObjectOutputStream(socket.getOutputStream());
                try {
                    while (true) {
                        File file = (File) input.readObject();
                        Log(file.getName());
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Log("ファイル通信エラーが発生しました[1] -> " + socket.getLocalAddress());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "FileClient");
    }

    public void sendPlayerFile(String uuid) {
        try {
            File file = new File(DataBasePath, "PlayerData/" + uuid + ".yml");
            output.writeObject(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}