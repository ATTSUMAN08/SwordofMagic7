package swordofmagic7.Classes

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import swordofmagic7.Data.DataBase
import swordofmagic7.Data.PlayerData
import swordofmagic7.Item.ItemStackData
import swordofmagic7.Skill.SkillData
import swordofmagic7.Sound.SoundList

import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.mutableMapOf

import swordofmagic7.Data.DataBase.getClassData
import swordofmagic7.Function.decoText
import swordofmagic7.Function.decoLore
import swordofmagic7.Function.equalInv
import swordofmagic7.Function.decoInv
import swordofmagic7.Sound.CustomSound.playSound

class Classes(private val player: Player, private val playerData: PlayerData) {
    companion object {
        const val MaxSlot = 4
        const val MaxLevel = 25
        val SlotReqLevel = intArrayOf(1, 10, 30, 50)
        val defaultClass = getClassData("Novice")

        var ReqExp: IntArray? = null
        fun ReqExp(Level: Int): Int {
            if (ReqExp == null) {
                ReqExp = IntArray(PlayerData.MaxLevel + 1)
                for (level in ReqExp!!.indices) {
                    var reqExp = 100f
                    reqExp *= Math.pow(level.toDouble(), 1.8)
                    reqExp *= Math.ceil(level / 10f.toDouble())
                    if (level >= 30) reqExp *= 2
                    if (level >= 50) reqExp *= 4
                    if (level >= 60) reqExp *= 4
                    if (level >= 65) reqExp *= 2
                    ReqExp!![level] = Math.round(reqExp).toInt()
                }
            }
            return if (Level < 0) 100 else if (Level > PlayerData.MaxLevel) Int.MAX_VALUE else ReqExp!![Level]
        }
    }

    private val ClassLevel = mutableMapOf<ClassData, Int>()
    private val ClassExp = mutableMapOf<ClassData, Int>()
    var classSlot = arrayOfNulls<ClassData>(MaxSlot)

    init {
        for (classData in DataBase.getClassList().values) {
            ClassLevel[classData] = 1
            ClassExp[classData] = 0
        }
        classSlot[0] = defaultClass
    }

    fun lastClass(): ClassData {
        var lastClass = defaultClass
        for (classData in classSlot) {
            if (classData != null) {
                lastClass = classData
            }
        }
        return lastClass
    }

    fun setClassLevel(classData: ClassData, level: Int) {
        ClassLevel[classData] = level
    }

    fun addClassLevel(classData: ClassData, addLevel: Int) {
        ClassLevel[classData] = getClassLevel(classData) + addLevel
        if (getClassLevel(classData) >= MaxLevel) {
            setClassExp(classData, 0)
        }
    }

    fun getClassLevel(classData: ClassData): Int {
        if (ClassLevel.getOrDefault(classData, 0) <= 0) {
            ClassLevel[classData] = 1
        }
        return ClassLevel[classData]!!
    }

    fun setClassExp(classData: ClassData, exp: Int) {
        ClassExp[classData] = exp
    }

    @Synchronized
    fun addClassExp(classData: ClassData, addExp: Int) {
        if (getClassLevel(classData) >= MaxLevel) {
            ClassExp[classData] = 0
        } else {
            ClassExp[classData] = getClassExp(classData) + addExp
        }
        val Level = getClassLevel(classData)
        if (ReqExp(Level) <= getClassExp(classData)) {
            var addLevel = 0
            while (ReqExp(Level + addLevel) <= getClassExp(classData)) {
                removeExp(classData, ReqExp(Level + addLevel))
                addLevel++
            }
            addClassLevel(classData, addLevel)
            Function.BroadCast(playerData.getNick() + "§aさんの§e[" + classData.Display.replace("§l", "") + "§e]§aが§eLv" + getClassLevel(classData) + "§aになりました", true)
            playSound(player, SoundList.LevelUp)
        }
        if (playerData.ExpLog) player.sendMessage("§e経験値[" + classData.Color + classData.Display + "§e]§7: §a+" + addExp + " §7(" + String.format(Function.format, addExp.toDouble() / Classes.ReqExp(getClassLevel(classData)) * 100) + "%)")
    }

    fun removeExp(classData: ClassData, addExp: Int) {
        ClassExp[classData] = getClassExp(classData) - addExp
    }

    fun getClassExp(classData: ClassData): Int {
        if (!ClassExp.containsKey(classData)) {
            ClassExp[classData] = 0
        }
        return ClassExp[classData]!!
    }

    fun viewExpPercent(classData: ClassData): String {
        return String.format("%.3f", getClassExp(classData).toFloat() / ReqExp(getClassLevel(classData)) * 100) + "%"
    }

    fun getPassiveSkillList(): Set<SkillData> {
        val list = mutableSetOf<SkillData>()
        for (classData in classSlot) {
            if (classData != null) {
                for (skillData in classData.SkillList) {
                    if (skillData.SkillType.isPassive) {
                        list.add(skillData)
                    }
                }
            }
        }
        return list
    }

    fun getActiveSkillList(): Set<SkillData> {
        val list = mutableSetOf<SkillData>()
        for (classData in classSlot) {
            if (classData != null) {
                for (skillData in classData.SkillList) {
                    if (!skillData.SkillType.isActive) {
                        list.add(skillData)
                    }
                }
            }
        }
        return list
    }

    fun ClassChange(classData: ClassData, slot: Int) {
        var changeAble = true
        val reqText = ArrayList<String>()
        for ((key, value) in classData.ReqClass) {
            if (playerData.Classes.getClassLevel(key) < value) {
                changeAble = false
                reqText.add("§7・" + key.getDisplay(true) + " Lv" + value + " §c✖")
            } else {
                reqText.add("§7・" + key.getDisplay(true) + " Lv" + value + " §b✔")
            }
        }
        if (changeAble) {
            classSlot[slot] = classData
            player.sendMessage("§e[クラススロット" + (slot + 1) + "]§aを" + classData.getDisplay(true, true) + "§aに§b転職§aしました")
            playerData.EffectManager.clearEffect()
            val petSummoned = playerData.PetSummon.size
            for (i in 0 until petSummoned) {
                playerData.PetSummon.first.cage()
            }
            playSound(player, SoundList.LevelUp)
        } else {
            player.sendMessage(decoText("§c転職条件"))
            for (str in reqText) {
                player.sendMessage(str)
            }
            playSound(player, SoundList.Nope)
        }
    }

    private val ClassSelectCache = arrayOfNulls<ClassData>(54)
    private var SelectSlot = -1
    fun ClassSelectView(isSlotMenu: Boolean) {
        var size = 6
        if (isSlotMenu) size = 1
        val inv = decoInv("クラスカウンター", size)
        if (isSlotMenu) {
            SelectSlot = -1
            for (i in SlotReqLevel.indices) {
                val lore = ArrayList<String>()
                lore.add(decoLore("必要レベル") + SlotReqLevel[i])
                if (classSlot[i] != null) {
                    lore.add(decoLore("使用状況") + classSlot[i]!!.getDisplay())
                } else {
                    lore.add(decoLore("使用状況") + "§7§l未使用")
                }
                inv.setItem(i, ItemStackData(Material.END_CRYSTAL, decoText("クラススロット[" + (i + 1) + "]"), lore).view())
            }
        } else {
            for ((key, value) in DataBase.ClassDataMap) {
                val classData = getClassData(value)
                ClassSelectCache[key] = classData
                inv.setItem(key, classData.view())
            }
        }
        playSound(player, SoundList.Click)
        player.openInventory(inv)
    }

    fun ClassSelectClick(view: InventoryView, slot: Int) {
        if (equalInv(view, "クラスカウンター")) {
            if (SelectSlot == -1 && slot < classSlot.size) {
                if (SlotReqLevel[slot] <= playerData.Level) {
                    SelectSlot = slot
                    ClassSelectView(false)
                } else {
                    player.sendMessage("§aレベルが足りません")
                    playSound(player, SoundList.Nope)
                }
            } else if (ClassSelectCache[slot] != null) {
                for (classData in classSlot) {
                    if (classData == ClassSelectCache[slot]) {
                        player.sendMessage("§aすでに[" + classData!!.Color + classData.Display + "]§aは使用されています")
                        playSound(player, SoundList.Nope)
                        return
                    }
                }
                val classData = ClassSelectCache[slot]
                ClassChange(classData!!, SelectSlot)
            }
        }
    }

    fun topClass(): ClassData? {
        return classSlot[0]
    }
}
