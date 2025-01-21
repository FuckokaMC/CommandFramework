package mc.fuckoka.commandframework

import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

abstract class SubCommandBase(
    val name: String,
    val usage: String,
    val permission: String,
    val aliases: Array<String> = arrayOf()
) : TabExecutor {
    fun testPermission(sender: CommandSender): Boolean {
        if (permission.isEmpty()) return true

        permission.split(";").forEach {
            if (sender.hasPermission(it)) return true
        }

        return false
    }
}
