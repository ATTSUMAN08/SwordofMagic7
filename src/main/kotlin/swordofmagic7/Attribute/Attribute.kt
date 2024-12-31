package swordofmagic7.Attribute

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import swordofmagic7.Data.DataBase
import swordofmagic7.Data.PlayerData
import swordofmagic7.Data.TitleData
import swordofmagic7.Function
import swordofmagic7.Item.ItemStackData
import swordofmagic7.Sound.SoundList

import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.mutableMapOf

import swordofmagic7.Function.decoText
import swordofmagic7.Function.decoLore
import swordofmagic7.Function.equalInv
import swordofmagic7.Function.decoInv
import swordofmagic7.Menu.Data.AttributeMenuDisplay
import swordofmagic7.Sound.CustomSound.playSound

class Attribute(private val player: Player, private val playerData: PlayerData) {
    private val Parameter = mutableMapOf<AttributeType, Int>()
    private var AttributePoint = 0

    init {
        for (attr in AttributeType.values()) {
            Parameter[attr] = 0
        }
    }

    fun getAttribute(): Map<AttributeType, Int> {
        return Parameter
    }

    fun getAttributePoint(): Int {
        return AttributePoint
    }

    fun getAttribute(type: AttributeType): Int {
        return Parameter[type] ?: 0
    }

    fun addPoint(add: Int) {
        AttributePoint += add
    }

    fun setPoint(point: Int) {
        AttributePoint = point
    }

    fun addAttribute(type: AttributeType, add: Int) {
        if (getAttributePoint() >= add) {
            AttributePoint -= add
            Parameter[type] = Parameter[type]!! + add
        } else {
            player.sendMessage("§eポイント§aが足りません")
        }
    }

    fun revAttribute(type: AttributeType, rev: Int) {
        if (Parameter[type]!! >= rev) {
            AttributePoint += rev
            Parameter[type] = Parameter[type]!! - rev
        } else {
            player.sendMessage("§eポイント§aが足りません")
        }
    }

    fun setAttribute(type: AttributeType, attr: Int) {
        Parameter[type] = attr
    }

    fun resetAttribute() {
        for (attr in AttributeType.values()) {
            Parameter[attr] = 0
        }
        AttributePoint = (playerData.Level - 1) * 5
        for (data in playerData.titleManager.TitleList) {
            val titleData = DataBase.TitleDataList[data]
            AttributePoint += titleData!!.attributePoint
        }
    }

    fun attributeView(type: AttributeType): ItemStack {
        val item = ItemStack(type.Icon)
        val meta = item.itemMeta
        meta.displayName(Component.text(decoText("${type.Display} §e§l[${Parameter[type]}] ")))
        val Lore = ArrayList(type.Lore)
        Lore.add(decoText("§3§l追加ステータス"))
        val format = "%.1f"
        when (type) {
            AttributeType.STR -> {
                Lore.add(decoLore("物理与ダメージ") + "+" + String.format(format, Parameter[type]!! * 0.5) + "%")
                Lore.add(decoLore("攻撃力") + "+" + String.format(format, Parameter[type]!! * 2.5) + "%")
            }
            AttributeType.INT -> {
                Lore.add(decoLore("魔法与ダメージ") + "+" + String.format(format, Parameter[type]!! * 0.4) + "%")
                Lore.add(decoLore("魔法被ダメージ軽減") + "+" + String.format(format, Parameter[type]!! * 0.1) + "%")
                Lore.add(decoLore("攻撃力") + "+" + String.format(format, Parameter[type]!! * 2.5) + "%")
            }
            AttributeType.DEX -> {
                Lore.add(decoLore("回避") + "+" + String.format(format, Parameter[type]!! * 4.2) + "%")
                Lore.add(decoLore("クリティカルダメージ") + "+" + String.format(format, Parameter[type]!! * 0.8) + "%")
            }
            AttributeType.TEC -> {
                Lore.add(decoLore("命中") + "+" + String.format(format, Parameter[type]!! * 4.2) + "%")
                Lore.add(decoLore("クリティカル発生") + "+" + String.format(format, Parameter[type]!! * 4.2) + "%")
            }
            AttributeType.SPI -> {
                Lore.add(decoLore("最大マナ") + "+" + String.format(format, Parameter[type]!! * 0.8) + "%")
                Lore.add(decoLore("マナ自動回復") + "+" + String.format(format, Parameter[type]!! * 0.05) + "%")
                Lore.add(decoLore("治癒力") + "+" + String.format(format, Parameter[type]!! * 2.5) + "%")
                Lore.add(decoLore("魔法被ダメージ軽減") + "+" + String.format(format, Parameter[type]!! * 0.1) + "%")
            }
            AttributeType.VIT -> {
                Lore.add(decoLore("最大体力") + "+" + String.format(format, Parameter[type]!! * 0.8) + "%")
                Lore.add(decoLore("防御力") + "+" + String.format(format, Parameter[type]!! * 2.5) + "%")
                Lore.add(decoLore("クリティカル耐性") + "+" + String.format(format, Parameter[type]!! * 3.8) + "%")
                Lore.add(decoLore("物理被ダメージ軽減") + "+" + String.format(format, Parameter[type]!! * 0.3) + "%")
                Lore.add(decoLore("魔法被ダメージ軽減") + "+" + String.format(format, Parameter[type]!! * 0.1) + "%")
            }
        }
        meta.lore(Lore.map { Component.text(it) })
        item.itemMeta = meta
        return item
    }

    private var AttributeMenuCache: Inventory? = null

    fun AttributeMenuView() {
        AttributeMenuCache = decoInv(AttributeMenuDisplay, 1)
        AttributeMenuLoad()
        player.openInventory(AttributeMenuCache!!)
    }

    fun AttributeMenuLoad() {
        val attribute = playerData.Attribute
        var slot = 0
        for (attr in AttributeType.values()) {
            AttributeMenuCache!!.setItem(slot, attribute.attributeView(attr))
            slot++
        }
        val lore = ArrayList<String>()
        lore.add(decoLore("ポイント") + attribute.getAttributePoint())
        lore.add("")
        lore.add("§c§l※クリックでアトリビュートをリセット")
        val point = ItemStackData(Material.EXPERIENCE_BOTTLE, decoText("アトリビュート"), lore).view()
        AttributeMenuCache!!.setItem(8, point)
    }

    fun AttributeMenuClick(view: InventoryView, clickType: ClickType, currentItem: ItemStack) {
        if (equalInv(view, AttributeMenuDisplay)) {
            val attr = playerData.Attribute
            for (attrType in AttributeType.values()) {
                if (currentItem.type == attrType.Icon) {
                    val x = if (clickType.isShiftClick) 10 else 1
                    if (clickType.isLeftClick) {
                        attr.addAttribute(attrType, x)
                    } else if (clickType.isRightClick) {
                        if (playerData.Map.Safe) {
                            attr.revAttribute(attrType, x)
                        } else Function.sendMessage(player, "§eセーフゾーン§aでのみ使用可能です", SoundList.Nope)
                    }
                }
            }
            if (currentItem.type == Material.EXPERIENCE_BOTTLE) {
                if (playerData.Map.Safe) {
                    attr.resetAttribute()
                } else Function.sendMessage(player, "§eセーフゾーン§aでのみ使用可能です", SoundList.Nope)
            }
            AttributeMenuLoad()
            playSound(player, SoundList.Click)
        }
    }
}
