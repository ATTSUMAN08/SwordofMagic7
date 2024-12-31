package swordofmagic7.Classes

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import swordofmagic7.Function
import swordofmagic7.Item.ItemStackData
import swordofmagic7.Skill.SkillData

import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.mutableMapOf

import swordofmagic7.Function.decoText

class ClassData {
    var Id: String? = null
    var Color: String? = null
    var Icon: Material? = null
    var Lore: List<String>? = null
    var Display: String? = null
    var Nick: String? = null
    var ProductionClass = false
    var SkillList: List<SkillData> = ArrayList()
    var ReqClass = mutableMapOf<ClassData, Int>()

    fun view(): ItemStack {
        val lore = Function.loreText(Lore)
        lore.add(decoText("§3§lスキル一覧"))
        for (skill in SkillList) {
            lore.add("§7・§e§l" + skill.Display)
        }
        lore.add(decoText("§3§l転職条件"))
        for ((key, value) in ReqClass) {
            lore.add("§7・§e§l" + key.Display + " Lv" + value)
        }
        return ItemStackData(Icon, decoText(Color + Display), lore).view()
    }

    fun getDisplay(): String {
        return getDisplay(true)
    }

    fun getDisplay(bold: Boolean): String {
        return getDisplay(bold, false)
    }

    fun getDisplay(bold: Boolean, brackets: Boolean): String {
        var _return = Display
        if (brackets) _return = "[$_return]"
        if (bold) _return = "§l$_return"
        return Color + _return
    }
}
