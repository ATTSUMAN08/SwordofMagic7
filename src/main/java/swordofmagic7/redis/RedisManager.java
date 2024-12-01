package swordofmagic7.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import swordofmagic7.Data.DataBase;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.SomCore;

import java.util.List;
import java.util.Objects;

public class RedisManager {
    public static JedisPool jedisPool = null;
    public static RedisSubscriber redisSubscriber = new RedisSubscriber("SNC");

    public static void connect(
            final String hostname,
            final int port,
            String username,
            String password,
            final boolean ssl
    ) {
        if (Objects.equals(username, "null")) username = null;
        if (Objects.equals(password, "null")) password = null;

        if (username != null) {
            jedisPool = new JedisPool(new JedisPoolConfig(), hostname, port, Protocol.DEFAULT_TIMEOUT, username, password, ssl);
        } else {
            jedisPool = new JedisPool(new JedisPoolConfig(), hostname, port, Protocol.DEFAULT_TIMEOUT, password, ssl);
        }
        MultiThread.TaskRun(redisSubscriber, "RedisSubscriber");
    }

    public static void publishObject(String channel, List<String> message) {
        try (Jedis jedis = getJedis()) {
            jedis.publish(channel, SomCore.gson.toJson(new RedisMessageObject(DataBase.ServerId, message)));
        }
    }

    public static Jedis getJedis() {
        if (jedisPool == null) {
            throw new NullPointerException("Redisに接続されていません");
        }
        return jedisPool.getResource();
    }
}
