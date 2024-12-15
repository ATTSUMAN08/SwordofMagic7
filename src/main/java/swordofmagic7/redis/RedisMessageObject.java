package swordofmagic7.redis;

public class RedisMessageObject {
    public final String identifier;
    public final String message;

    public RedisMessageObject(String identifier, String message) {
        this.identifier = identifier;
        this.message = message;
    }
}
