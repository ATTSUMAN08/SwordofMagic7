package net.somrpg.swordofmagic7.npc

import com.charleskorn.kaml.Yaml
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.somrpg.swordofmagic7.SomCore
import net.somrpg.swordofmagic7.SomCore.Companion.instance
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.entity.Villager
import org.bukkit.persistence.PersistentDataType
import swordofmagic7.Data.DataBase
import java.io.File
import java.util.UUID

object NPCManager {
    val spawnedNPCs: MutableMap<UUID, NPCData.NPC> = mutableMapOf()

    fun spawnAllNPCs() {
        val file = File(DataBase.DataBasePath, "npcs.yml")
        if (!file.exists()) return
        val npcData = Yaml.default.decodeFromString(NPCData.serializer(), file.readText())
        for (i in npcData.npcs) {
            val locSplit = i.location.split(" ").map { it.toDouble() } // X Y Z Yaw Pitch
            val npc = spawn(
                Location(SomCore.world, locSplit[0], locSplit[1], locSplit[2], locSplit[3].toFloat(), locSplit[4].toFloat()),
                Registry.VILLAGER_PROFESSION[NamespacedKey.minecraft(i.profession.lowercase())] ?: Villager.Profession.NONE,
                Registry.VILLAGER_TYPE[NamespacedKey.minecraft(i.type.lowercase())] ?: Villager.Type.PLAINS,
                MiniMessage.miniMessage().deserialize(i.name)
            )
            spawnedNPCs[npc.uniqueId] = i
        }
    }

    fun spawn(loc: Location, profession: Villager.Profession, type: Villager.Type, customName: Component): Villager {
        val npc = loc.world.spawn(loc, Villager::class.java)
        npc.profession = profession
        npc.villagerType = type
        npc.isCustomNameVisible = true
        npc.customName(customName)
        npc.setAI(false)
        npc.setGravity(false)
        npc.isInvulnerable = true
        npc.isSilent = true
        npc.isCollidable = false
        npc.removeWhenFarAway = false
        npc.persistentDataContainer[NamespacedKey(instance, "som7entity"), PersistentDataType.BOOLEAN] = true
        return npc
    }

    fun isNPC(uuid: UUID): Boolean {
        return spawnedNPCs.containsKey(uuid)
    }
}