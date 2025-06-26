package net.somrpg.swordofmagic7.player.sidebar

import swordofmagic7.Data.DataBase
import swordofmagic7.Data.PlayerData
import swordofmagic7.Function
import swordofmagic7.Life.LifeType

class SideBarToDo(
    private val playerData: PlayerData
) {
    private val dataList: MutableList<SideBarToDoData> = mutableListOf()

    fun clear() {
        dataList.clear()
        playerData.player.sendMessage("§eSideBarToDoのデータを§cクリア§eしました")
    }

    fun toggleItemAmount(itemName: String) {
        if (!DataBase.ItemList.containsKey(itemName)) {
            playerData.player.sendMessage("§e$itemName§aは存在しない§eアイテム§aです")
            return
        }

        if (dataList.any { it.type == SideBarToDoType.ITEM_AMOUNT && it.key == itemName }) {
            dataList.removeIf { it.type == SideBarToDoType.ITEM_AMOUNT && it.key == itemName }
            playerData.player.sendMessage("§e${itemName}のアイテム個数表示を§c無効§eにしました")
        } else {
            dataList.add(SideBarToDoData(SideBarToDoType.ITEM_AMOUNT, itemName))
            playerData.player.sendMessage("§e${itemName}のアイテム個数表示を§a有効§eにしました")
        }
    }

    fun toggleRecipeInfo(recipeId: String) {
        if (!DataBase.ItemRecipeList.containsKey(recipeId)) {
            playerData.player.sendMessage("§e$recipeId§aは存在しない§eレシピ§aです")
            return
        }

        for (i in DataBase.getItemRecipe(recipeId).ReqStack) {
            toggleItemAmount(i.itemParameter.Id)
        }
    }

    fun toggleLifeInfo(lifeId: String) {
        val lifeType = LifeType.getData(lifeId)
        if (lifeType == null) {
            playerData.player.sendMessage("§e$lifeId§aは存在しない§e生活ステータス§aです")
            return
        }

        if (dataList.any { it.type == SideBarToDoType.LIFE_INFO && it.key == lifeId }) {
            dataList.removeIf { it.type == SideBarToDoType.LIFE_INFO && it.key == lifeId }
            playerData.player.sendMessage("§e${lifeType.Display}の生活ステータス表示を§c無効§eにしました")
        } else {
            dataList.add(SideBarToDoData(SideBarToDoType.LIFE_INFO, lifeId))
            playerData.player.sendMessage("§e${lifeType.Display}の生活ステータス表示を§a有効§eにしました")
        }
    }

    fun toggleClassInfo(classId: String) {
        val classData = DataBase.getClassData(classId)
        if (classData == null) {
            playerData.player.sendMessage("§e$classId§aは存在しない§eクラス§aです")
            return
        }

        if (dataList.any { it.type == SideBarToDoType.CLASS_INFO && it.key == classId }) {
            dataList.removeIf { it.type == SideBarToDoType.CLASS_INFO && it.key == classId }
            playerData.player.sendMessage("§e${classData.Display}のクラス情報表示を§c無効§eにしました")
        } else {
            dataList.add(SideBarToDoData(SideBarToDoType.CLASS_INFO, classId))
            playerData.player.sendMessage("§e${classData.Display}のクラス情報表示を§a有効§eにしました")
        }
    }

    fun refresh() {
        if (dataList.isEmpty()) {
            playerData.ViewBar.resetSideBar("SideBarToDo")
            return
        }

        val lines: MutableList<String> = mutableListOf()
        lines.add(Function.decoText("SideBarToDo"))
        for (data in dataList) {
            val key = data.key
            when (data.type) {
                SideBarToDoType.ITEM_AMOUNT -> {
                    val itemParameter = DataBase.getItemParameter(key) ?: continue
                    val amount = playerData.ItemInventory.getItemParameterStack(itemParameter).Amount
                    lines.add(Function.decoLore("アイテム数[" + itemParameter.Display + "]") + amount + "個")
                }
                SideBarToDoType.LIFE_INFO -> {
                    val lifeType = LifeType.getData(key) ?: continue
                    lines.add("§7・§e§l" + lifeType.Display + " Lv" + playerData.LifeStatus.getLevel(lifeType) + " " + playerData.LifeStatus.viewExpPercent(lifeType))
                }
                SideBarToDoType.CLASS_INFO -> {
                    val classData = DataBase.getClassData(key) ?: continue
                    lines.add("§7・" + classData.Color + "§l" + classData.Display + " §e§lLv" + playerData.Classes.getClassLevel(classData) + " §a§l" + playerData.Classes.viewExpPercent(classData))
                }
            }
        }
        playerData.ViewBar.setSideBar("SideBarToDo", lines)
    }
}