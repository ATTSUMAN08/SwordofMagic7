package swordofmagic7.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import swordofmagic7.Data.DataBase;

import java.util.List;

@SuppressWarnings("unused")
public class RedisMessageEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final String channel;
    private final String identifier;
    private final List<String> message;

    public RedisMessageEvent(final String channel, final String identifier, final List<String> message) {
        super(true);
        this.channel = channel;
        this.message = message;
        this.identifier = identifier;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public String getChannel() {
        return channel;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<String> getMessage() {
        return message;
    }

    public Boolean isSelfSender() {
        return this.identifier.equals(DataBase.ServerId);
    }
}