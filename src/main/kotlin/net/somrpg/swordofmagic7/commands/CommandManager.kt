package net.somrpg.swordofmagic7.commands

import me.attsuman08.abysslib.shade.acf.*
import net.somrpg.swordofmagic7.SomCore
import net.somrpg.swordofmagic7.extensions.getPlayerData
import net.somrpg.swordofmagic7.utils.PackageClassFinder
import org.bukkit.Bukkit
import swordofmagic7.Data.DataBase
import swordofmagic7.Life.LifeType
import java.util.*

object CommandManager {

    fun registerCommands() {
        val manager = PaperCommandManager(SomCore.instance)
        manager.locales.defaultLocale = Locale.JAPANESE

        registerConditions(manager)
        registerCompletions(manager)

        // コマンドを登録する
        PackageClassFinder.getClasses("net.somrpg.swordofmagic7.commands").forEach { clazz ->
            try {
                if (BaseCommand::class.java.isAssignableFrom(clazz)) {
                    val command = clazz.getDeclaredConstructor().newInstance() as BaseCommand
                    manager.registerCommand(command)
                }
            } catch (e: Exception) {
                SomCore.instance.logger.warning("[CommandManager] ${clazz.name}のコマンドの登録に失敗しました: ${e.message}")
            }
        }
    }

    private fun registerConditions(manager: PaperCommandManager) {
        // Int | limits
        manager.commandConditions.addCondition(Int::class.java, "limits",
            CommandConditions.ParameterCondition { c: ConditionContext<BukkitCommandIssuer>, _: BukkitCommandExecutionContext, value: Int? ->
                if (value == null) {
                    return@ParameterCondition
                }
                if (c.hasConfig("min") && c.getConfigValue("min", 0) > value) {
                    throw ConditionFailedException("値の最低値は${c.getConfigValue("min", 0)}です")
                }
                if (c.hasConfig("max") && c.getConfigValue("max", 3) < value) {
                    throw ConditionFailedException("値の最高値は${c.getConfigValue("max", 3)}です")
                }
            }
        )

        // Float | limits
        manager.commandConditions.addCondition(Float::class.java, "limits",
            CommandConditions.ParameterCondition { c: ConditionContext<BukkitCommandIssuer>, _: BukkitCommandExecutionContext, value: Float? ->
                if (value == null) {
                    return@ParameterCondition
                }
                val min = c.getConfigValue("min", "0").toFloat()
                val max = c.getConfigValue("max", "3").toFloat()
                if (c.hasConfig("min") && min > value) {
                    throw ConditionFailedException("値の最低値は${min}です")
                }
                if (c.hasConfig("max") && max < value) {
                    throw ConditionFailedException("値の最高値は${max}です")
                }
            }
        )
    }

    private fun registerCompletions(manager: PaperCommandManager) {
        manager.commandCompletions.registerCompletion("players") { _ ->
            return@registerCompletion Bukkit.getOnlinePlayers().map { it.name }
        }

        manager.commandCompletions.registerCompletion("channels") { _ ->
            return@registerCompletion listOf("1", "2", "3", "event")
        }

        manager.commandCompletions.registerCompletion("reloadable") { _ ->
            return@registerCompletion listOf("all", "title", "item", "rune", "skill", "shop")
        }

        manager.commandCompletions.registerCompletion("items") { context ->
            val playerData = context.player.getPlayerData()
            return@registerCompletion playerData.ItemInventory.list.map { it.itemParameter.Id }
        }

        manager.commandCompletions.registerCompletion("recipes") { _ ->
            return@registerCompletion DataBase.getItemRecipeList().keys
        }

        manager.commandCompletions.registerCompletion("lifes") { _ ->
            return@registerCompletion LifeType.entries.map { it.name }
        }

        manager.commandCompletions.registerCompletion("classes") { _ ->
            return@registerCompletion DataBase.getClassList().keys
        }
    }

}