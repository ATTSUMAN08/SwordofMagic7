package net.somrpg.swordofmagic7.commands.user

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import kotlinx.coroutines.withContext
import net.somrpg.swordofmagic7.SomCore
import org.bukkit.command.CommandSender
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission

@Suppress("unused")
class TestSomCommand {

    @Command("testsom")
    @Permission("som.developer")
    suspend fun testSom(sender: CommandSender): Unit = withContext(SomCore.instance.asyncDispatcher) {

    }
}