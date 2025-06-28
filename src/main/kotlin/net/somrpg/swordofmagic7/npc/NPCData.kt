@file:Suppress("PROVIDED_RUNTIME_TOO_LOW", "INLINE_CLASSES_NOT_SUPPORTED")
package net.somrpg.swordofmagic7.npc

import kotlinx.serialization.Serializable

@Serializable
data class NPCData(
    val npcs: List<NPC> = emptyList(),
) {

    @Serializable
    data class NPC(
        val name: String,
        val location: String,
        val profession: String = "none",
        val type: String = "plains",
        val messageId: Int = -1, // -1はメッセージなし
    )
}