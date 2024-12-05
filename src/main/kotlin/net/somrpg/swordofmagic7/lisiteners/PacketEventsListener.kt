package net.somrpg.swordofmagic7.lisiteners

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import swordofmagic7.Data.PlayerData
import swordofmagic7.Life.Gathering.ChangeBlock

class PacketEventsListener : PacketListener {

    override fun onPacketSend(e: PacketSendEvent) {
        val p: Player = e.getPlayer() ?: return
        if (!p.isOnline) return

        when (e.packetType) {
            PacketType.Play.Server.BLOCK_CHANGE -> {
                val packet = WrapperPlayServerBlockChange(e)
                val loc = Location(p.world, packet.blockPosition.x.toDouble(), packet.blockPosition.y.toDouble(), packet.blockPosition.z.toDouble())
                if (p.gameMode != GameMode.CREATIVE && ChangeBlock(p).checkLocation(loc)) {
                    val material = ChangeBlock(p).get(loc)
                    packet.blockState = WrappedBlockState.getDefaultState(StateTypes.getByName(material.name))
                }
            }
            PacketType.Play.Server.PARTICLE, PacketType.Play.Server.STOP_SOUND, PacketType.Play.Server.ENTITY_SOUND_EFFECT,
            PacketType.Play.Server.SOUND_EFFECT, PacketType.Play.Server.NAMED_SOUND_EFFECT -> {
                val playerData = PlayerData.playerData(p) ?: return
                if (playerData.isAFK) {
                    e.isCancelled = true
                }
            }
        }
    }
}