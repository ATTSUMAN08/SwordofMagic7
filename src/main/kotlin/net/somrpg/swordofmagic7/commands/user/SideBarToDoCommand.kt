@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.user

import me.attsuman08.abysslib.shade.acf.BaseCommand
import me.attsuman08.abysslib.shade.acf.annotation.CommandAlias
import me.attsuman08.abysslib.shade.acf.annotation.CommandCompletion
import me.attsuman08.abysslib.shade.acf.annotation.CommandPermission
import me.attsuman08.abysslib.shade.acf.annotation.Default
import me.attsuman08.abysslib.shade.acf.annotation.Subcommand
import me.attsuman08.abysslib.shade.acf.annotation.Syntax
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player
import swordofmagic7.Function

@CommandAlias("sidebartodo|sbtd")
@CommandPermission("som7.user")
class SideBarToDoCommand : BaseCommand() {

    @Default
    fun default(player: Player) {
        player.sendMessage(Function.decoText("SideBarToDo Commands"))
        player.sendMessage("§e/sbtd itemAmount <ItemName>")
        player.sendMessage("§e/sbtd recipeInfo <RecipeId>")
        player.sendMessage("§e/sbtd lifeInfo <LifeID>")
        player.sendMessage("§e/sbtd classInfo <ClassID>")
        player.sendMessage("§e/sbtd clear")
    }

    @Subcommand("itemamount")
    @Syntax("<itemName>")
    @CommandCompletion("@items")
    fun itemAmount(player: Player, itemName: String) {
        player.getPlayerData().SideBarToDo.toggleItemAmount(itemName)
    }

    @Subcommand("recipeinfo")
    @Syntax("<recipeId>")
    @CommandCompletion("@recipes")
    fun recipeInfo(player: Player, recipeId: String) {
        player.getPlayerData().SideBarToDo.toggleRecipeInfo(recipeId)
    }

    @Subcommand("lifeinfo")
    @Syntax("<lifeId>")
    @CommandCompletion("@lifes")
    fun lifeInfo(player: Player, lifeId: String) {
        player.getPlayerData().SideBarToDo.toggleLifeInfo(lifeId)
    }

    @Subcommand("classinfo")
    @Syntax("<classId>")
    @CommandCompletion("@classes")
    fun classInfo(player: Player, classId: String) {
        player.getPlayerData().SideBarToDo.toggleClassInfo(classId)
    }

    @Subcommand("clear")
    fun clear(player: Player) {
        player.getPlayerData().SideBarToDo.clear()
    }
}