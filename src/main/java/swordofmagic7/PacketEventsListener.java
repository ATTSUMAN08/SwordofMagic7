package swordofmagic7;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;

import static swordofmagic7.Life.Gathering.ChangeBlock;

public class PacketEventsListener implements PacketListener {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.playerData(player);

        if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
            WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange(event);
            Location location = new Location(player.getWorld(), packet.getBlockPosition().getX(), packet.getBlockPosition().getY(), packet.getBlockPosition().getZ());
            if (player.getGameMode() != GameMode.CREATIVE) {
                if (ChangeBlock(player).checkLocation(location)) {
                    Material material = ChangeBlock(player).get(location);
                    packet.setBlockState(WrappedBlockState.getDefaultState(StateTypes.getByName(material.name())));
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Server.PARTICLE
                || event.getPacketType() == PacketType.Play.Server.STOP_SOUND
                || event.getPacketType() == PacketType.Play.Server.ENTITY_SOUND_EFFECT
                || event.getPacketType() == PacketType.Play.Server.SOUND_EFFECT
                || event.getPacketType() == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
            if (playerData.isAFK()) {
                event.setCancelled(true);
            }
        }
    }
}
