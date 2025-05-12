package net.somrpg.swordofmagic7.commands

import me.attsuman08.abysslib.shade.acf.*
import net.somrpg.swordofmagic7.SomCore
import net.somrpg.swordofmagic7.commands.builder.FlySpeedCommand
import net.somrpg.swordofmagic7.commands.builder.GmCommand
import net.somrpg.swordofmagic7.commands.builder.PlayModeCommand
import net.somrpg.swordofmagic7.commands.developer.SomCommand
import net.somrpg.swordofmagic7.commands.user.AttributeCommand
import net.somrpg.swordofmagic7.commands.user.settings.DamageHoloCommand
import net.somrpg.swordofmagic7.commands.user.settings.DamageLogCommand
import net.somrpg.swordofmagic7.commands.user.settings.DropLogCommand
import net.somrpg.swordofmagic7.commands.user.settings.EffectLogCommand
import net.somrpg.swordofmagic7.commands.user.settings.ExpLogCommand
import net.somrpg.swordofmagic7.commands.user.MenuCommand
import net.somrpg.swordofmagic7.commands.user.SkillCommand
import org.bukkit.Bukkit
import java.util.*

object CommandManager {

    fun registerCommands() {
        val manager = PaperCommandManager(SomCore.instance)
        manager.locales.defaultLocale = Locale.JAPANESE

        registerConditions(manager)
        registerCompletions(manager)

        // User Commands
        manager.registerCommand(MenuCommand())
        manager.registerCommand(SkillCommand())
        manager.registerCommand(AttributeCommand())
        manager.registerCommand(DamageHoloCommand())
        manager.registerCommand(DamageLogCommand())
        manager.registerCommand(ExpLogCommand())
        manager.registerCommand(DropLogCommand())
        manager.registerCommand(EffectLogCommand())

        // Builder Commands
        manager.registerCommand(GmCommand())
        manager.registerCommand(FlySpeedCommand())
        manager.registerCommand(PlayModeCommand())

        // Developer Commands
        manager.registerCommand(SomCommand())
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
    }
}