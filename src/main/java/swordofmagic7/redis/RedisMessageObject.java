package swordofmagic7.redis;

import java.util.List;

public class RedisMessageObject {
    public String identifier;
    public List<String> message;

    public RedisMessageObject(String identifier, List<String> message) {
        this.identifier = identifier;
        this.message = message;
    }
}
