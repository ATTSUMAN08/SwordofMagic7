package net.somrpg.swordofmagic7.player.attribute

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import swordofmagic7.Data.DataBase
import swordofmagic7.Data.PlayerData
import swordofmagic7.Data.TitleData
import swordofmagic7.Function
import swordofmagic7.Item.ItemStackData
import swordofmagic7.Menu.Data
import swordofmagic7.Sound.CustomSound
import swordofmagic7.Sound.SoundList
import java.text.NumberFormat
import java.util.EnumMap

class Attribute(
    private val player: Player,
    private val playerData: PlayerData,
) {
    companion object {
        private val numberFormat =
            NumberFormat.getNumberInstance().apply {
                maximumFractionDigits = 2
            }
    }

    val attributeMap = EnumMap<AttributeType, Int>(AttributeType::class.java)
    var attributePoint: Int = 0
        private set

    fun getAttribute(type: AttributeType?): Int = attributeMap[type]!!

    fun addPoint(add: Int) {
        this.attributePoint += add
    }

    fun setPoint(point: Int) {
        this.attributePoint = point
    }

    fun addAttribute(
        type: AttributeType?,
        add: Int,
    ) {
        if (this.attributePoint >= add) {
            this.attributePoint -= add
            attributeMap[type] = attributeMap[type]!! + add
        } else {
            player.sendMessage("§eポイント§aが足りません")
        }
    }

    fun revAttribute(
        type: AttributeType?,
        rev: Int,
    ) {
        if (attributeMap[type]!! >= rev) {
            this.attributePoint += rev
            attributeMap[type] = attributeMap[type]!! - rev
        } else {
            player.sendMessage("§eポイント§aが足りません")
        }
    }

    fun setAttribute(
        type: AttributeType?,
        attr: Int,
    ) {
        attributeMap[type] = attr
    }

    fun resetAttribute() {
        for (attr in AttributeType.entries) {
            attributeMap[attr] = 0
        }
        this.attributePoint = (playerData.Level - 1) * 5
        for (data in playerData.titleManager.TitleList) {
            val titleData: TitleData = DataBase.TitleDataList[data]!!
            this.attributePoint += titleData.attributePoint
        }
    }

    @Suppress("UnstableApiUsage")
    fun attributeView(type: AttributeType): ItemStack {
        val item = ItemStack.of(type.icon)
        val lore = mutableListOf<String>()
        lore.add(type.lore)
        lore.add(Function.decoText("§3§l追加ステータス"))
        for (stat in type.stats) {
            val value = stat.value * attributeMap[type]!!
            lore.add("${Function.decoLore(stat.key.Display)}+${numberFormat.format(value)}%")
        }
        item.setData(
            DataComponentTypes.ITEM_NAME,
            Component.text(Function.decoText(type.display + " §e§l[" + attributeMap[type] + "] ")),
        )
        item.setData(DataComponentTypes.LORE, ItemLore.lore(lore.map { Component.text(it) }))
        return item
    }

    private var attributeMenuCache: Inventory? = null

    init {
        for (attr in AttributeType.entries) {
            attributeMap[attr] = 0
        }
    }

    fun attributeMenuView() {
        attributeMenuCache = Function.decoInv(Data.AttributeMenuDisplay, 1)
        attributeMenuLoad()
        player.openInventory(attributeMenuCache!!)
    }

    fun attributeMenuLoad() {
        val attribute = playerData.Attribute
        for ((slot, attr) in AttributeType.entries.withIndex()) {
            attributeMenuCache!!.setItem(slot, attribute.attributeView(attr))
        }
        val lore: MutableList<String?> = ArrayList()
        lore.add(Function.decoLore("ポイント") + attribute.attributePoint)
        lore.add("")
        lore.add("§c§l※クリックでアトリビュートをリセット")
        val point = ItemStackData(Material.EXPERIENCE_BOTTLE, Function.decoText("アトリビュート"), lore).view()
        attributeMenuCache!!.setItem(8, point)
    }

    fun attributeMenuClick(
        view: InventoryView,
        clickType: ClickType,
        currentItem: ItemStack,
    ) {
        if (!Function.equalInv(view, Data.AttributeMenuDisplay)) return

        val attr = playerData.Attribute
        for (attrType in AttributeType.entries) {
            if (currentItem.type != attrType.icon) continue

            val x = if (clickType.isShiftClick) 10 else 1
            if (clickType.isLeftClick) {
                attr.addAttribute(attrType, x)
            } else if (clickType.isRightClick) {
                if (playerData.Map.Safe) {
                    attr.revAttribute(attrType, x)
                } else {
                    Function.sendMessage(player, "§eセーフゾーン§aでのみ使用可能です", SoundList.NOPE)
                }
            }
        }
        if (currentItem.type == Material.EXPERIENCE_BOTTLE) {
            if (playerData.Map.Safe) {
                attr.resetAttribute()
            } else {
                Function.sendMessage(player, "§eセーフゾーン§aでのみ使用可能です", SoundList.NOPE)
            }
        }
        attributeMenuLoad()
        CustomSound.playSound(player, SoundList.CLICK)
    }
}
