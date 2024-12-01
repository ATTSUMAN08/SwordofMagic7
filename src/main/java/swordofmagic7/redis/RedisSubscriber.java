package swordofmagic7.redis;

import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import swordofmagic7.MultiThread.MultiThreadRunnable;
import swordofmagic7.SomCore;
import swordofmagic7.api.events.RedisMessageEvent;

import java.util.Arrays;

public class RedisSubscriber extends JedisPubSub implements MultiThreadRunnable {

    private final String[] channels;

    public RedisSubscriber(String... channels) {
        this.channels = channels;
    }

    @Override
    public void run() {
        boolean firstTry = true;

        while (SomCore.plugin.isEnabled() && !Thread.interrupted() && RedisManager.jedisPool != null && !RedisManager.jedisPool.isClosed()) {
            try (Jedis jedis = RedisManager.getJedis()) {
                if (firstTry) {
                    SomCore.plugin.getLogger().info("Redis Pub/Sub接続が確立されました!");
                    firstTry = false;
                } else {
                    SomCore.plugin.getLogger().info("Redis Pub/Sub接続が再確立されました!");
                }

                try {
                    SomCore.plugin.getLogger().info("正常にRedisチャンネルに登録しました: " + Arrays.toString(channels));
                    jedis.subscribe(this, channels); // blocking call
                } catch (Exception e) {
                    SomCore.plugin.getLogger().warning("Redisチャンネルの登録中にエラーが発生しました: " + e.getMessage());
                }
            } catch (Exception e) {
                if (!SomCore.plugin.isEnabled()) {
                    return;
                }

                SomCore.plugin.getLogger().warning("Redis pubsub connection dropped, trying to re-open the connection!, " + e.getMessage());
                try {
                    unsubscribe();
                } catch (Exception ignored) {}

                try {
                    // noinspection BusyWait
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void onMessage(String channel, String message) {
        RedisMessageObject obj = SomCore.gson.fromJson(message, RedisMessageObject.class);
        if (obj == null) {
            SomCore.plugin.getLogger().warning("無効なメッセージをRedisから受信しました: " + message);
            return;
        }
        Bukkit.getServer().getPluginManager().callEvent(new RedisMessageEvent(channel, obj.identifier, obj.message));
    }
}
