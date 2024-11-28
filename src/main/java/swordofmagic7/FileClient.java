package swordofmagic7;

import swordofmagic7.Data.PlayerData;
import swordofmagic7.MultiThread.MultiThread;

import java.io.*;
import java.net.Socket;

import static swordofmagic7.Function.Log;

public class FileClient {
    static Socket socket;
    static boolean isConnection = false;
    static InputStream in;
    static OutputStream out;

    public static void connect() {
        MultiThread.TaskRun(() -> {
            FileOutputStream fos;
            try {
                socket = new Socket(Client.Host, 24457);
                isConnection = true;
                Log("ファイルサーバーセッション開始");

                in = socket.getInputStream();
                out = socket.getOutputStream();

                fos = new FileOutputStream(new File(SomCore.plugin.getDataFolder(), "client_receive.yml"));

                while (socket.isConnected()) {
                    int ch;
                    while ((ch = in.read()) != 0) {
                        Log("" + ch);
                        fos.write(ch);
                    }
                    MultiThread.sleepTick(1);
                }
            } catch (IOException e) {
                Log("ファイルサーバーエラーが発生しました[0]");
                if (isConnection) {
                    Log("ファイルサーバーから切断されました");
                    isConnection = false;
                }
                MultiThread.sleepTick(100);
                connect();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log("ファイルサーバーセッション終了");
            }
        }, "FileClient");
    }

    public static void requestPlayerData(PlayerData playerData) {
        MultiThread.TaskRun(() -> {
            int ch;
            try {
                FileInputStream fis = new FileInputStream(new File(SomCore.plugin.getDataFolder(), "client_send.yml"));
                while ((ch = fis.read()) != -1) {
                    out.write(ch);
                }
                out.flush();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            MultiThread.TaskRunSynchronized(playerData::load);
        }, "LoadFromFileServer");
    }
}
