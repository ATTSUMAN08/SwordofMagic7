package swordofmagic7.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import swordofmagic7.MultiThread.MultiThreadRunnable;
import swordofmagic7.SomCore;

import java.util.Arrays;

public class RedisSubscriber extends JedisPubSub implements MultiThreadRunnable {

    private final String[] channels;

    public RedisSubscriber(String... channels) {
        this.channels = channels;
    }

    @Override
    public void run() {
        boolean firstTry = true;

        while (!RedisManager.closing && !Thread.interrupted() && RedisManager.jedisPool != null && !RedisManager.jedisPool.isClosed()) {
            try (Jedis jedis = RedisManager.getJedis()) {
                if (firstTry) {
                    SomCore.plugin.getLogger().info("Redis Pub/Sub接続が確立されました!");
                    firstTry = false;
                } else {
                    SomCore.plugin.getLogger().info("Redis Pub/Sub接続が再確立されました!");
                }

                try {
                    jedis.subscribe(this, channels); // blocking call
                    SomCore.plugin.getLogger().info("Successfully subscribed channels: " + Arrays.toString(channels) + "!");
                } catch (Exception e) {
                    SomCore.plugin.getLogger().warning("Could not subscribe!");
                }
            } catch (Exception e) {
                if (RedisManager.closing) {
                    return;
                }

                SomCore.plugin.getLogger().warning("Redis pubsub connection dropped, trying to re-open the connection!, " + e.getMessage());
                try {
                    unsubscribe();
                } catch (Exception ignored) {}

                // Sleep for 5 seconds to prevent massive spam in console
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void onMessage(String channel, String message) {
        SomCore.plugin.getLogger().info("Received message from channel " + channel + ": " + message);
    }
}
