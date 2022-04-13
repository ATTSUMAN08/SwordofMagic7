package swordofmagic7;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import swordofmagic7.Data.PlayerData;

import static swordofmagic7.Life.Gathering.ChangeBlock;

public class PacketListener extends PacketAdapter {
    public PacketListener(Plugin plugin, PacketType... types) {
        super(plugin, types);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {

    }

    @Override
    public void onPacketSending(PacketEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.playerData(player);
        PacketContainer packet = event.getPacket();
        PacketType packetType = event.getPacketType();
        if (packetType == PacketType.Play.Server.BLOCK_CHANGE) {
            Location location = packet.getBlockPositionModifier().read(0).toLocation(player.getWorld());
            if (player.getGameMode() != GameMode.CREATIVE) {
                if (ChangeBlock(player).checkLocation(location)) {
                    BlockData blockData = ChangeBlock(player).get(location);
                    packet.getBlockData().write(0, WrappedBlockData.createData(blockData));
                }
            }
        } else if (packetType == PacketType.Play.Server.WORLD_PARTICLES
                || packetType == PacketType.Play.Server.STOP_SOUND
                || packetType == PacketType.Play.Server.ENTITY_SOUND
                || packetType == PacketType.Play.Server.CUSTOM_SOUND_EFFECT
                || packetType == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
            if (playerData.isAFK()) {
                event.setCancelled(true);
            }
        }
    }
}
