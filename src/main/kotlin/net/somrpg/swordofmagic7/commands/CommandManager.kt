package net.somrpg.swordofmagic7.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import net.somrpg.swordofmagic7.SomCore
import net.somrpg.swordofmagic7.commands.user.TestSomCommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.bukkit.CloudBukkitCapabilities
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.kotlin.coroutines.annotations.installCoroutineSupport
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler
import org.incendo.cloud.paper.LegacyPaperCommandManager
import org.incendo.cloud.suggestion.Suggestion
import java.util.concurrent.CompletableFuture

class CommandManager {

    fun registerCommands() {
        val commandManager = createCommandManager()

        registerMinecraftExceptionHandler(commandManager)
        registerSuggestionProviders(commandManager)

        val annotationParser = createAnnotationParser(commandManager)
        annotationParser.installCoroutineSupport(context = SomCore.instance.asyncDispatcher)

        annotationParser.parse(TestSomCommand())
    }

    private fun createCommandManager(): LegacyPaperCommandManager<CommandSender> {
        val commandManager = LegacyPaperCommandManager(
            SomCore.instance,
            ExecutionCoordinator.builder<CommandSender>().build(),
            SenderMapper.identity()
        )
        if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            (commandManager as LegacyPaperCommandManager<*>).registerAsynchronousCompletions()
        }
        return commandManager
    }

    private fun registerMinecraftExceptionHandler(commandManager: LegacyPaperCommandManager<CommandSender>) {
        MinecraftExceptionHandler.createNative<CommandSender>()
            .defaultHandlers()
            .decorator { component ->
                component
            }
            .registerTo(commandManager)
    }

    private fun registerSuggestionProviders(commandManager: LegacyPaperCommandManager<CommandSender>) {
        commandManager.parserRegistry().registerSuggestionProvider("players") { _, _ ->
            CompletableFuture.completedFuture(Bukkit.getOnlinePlayers().map { Suggestion.suggestion(it.name) })
        }
    }

    private fun createAnnotationParser(commandManager: LegacyPaperCommandManager<CommandSender>): AnnotationParser<CommandSender> {
        return AnnotationParser(commandManager, CommandSender::class.java)
    }
}