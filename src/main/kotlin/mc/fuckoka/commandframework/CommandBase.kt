package mc.fuckoka.commandframework

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

abstract class CommandBase : TabExecutor {
    private val subCommands = mutableMapOf<String, SubCommandBase>()

    // 苦し紛れのArray->Listで対応
    abstract fun onCommand(sender: CommandSender, command: Command, label: String, args: List<String>): Boolean

    abstract fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: List<String>
    ): MutableList<String>?

    final override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.isNotEmpty()) {
            val subCommand = subCommands[args[0]]
            if (subCommand != null) {
                // サブコマンドの実行権限を持っていない場合は終了
                if (!subCommand.testPermission(sender)) return true

                return subCommand.onCommand(sender, command, args[0], args.drop(1).toTypedArray())
            }
        }

        return onCommand(sender, command, label, args.toList())
    }

    final override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String>? {
        return when (args.size) {
            1 -> {
                val joined = mutableListOf<String>()
                val parent = onTabComplete(sender, command, label, args.toList()) ?: mutableListOf()
                val sub = subCommands.keys.filter { it.startsWith(args[0]) }.toMutableList()
                joined.addAll(parent)
                joined.addAll(sub)
                joined
            }

            else -> {
                // サブコマンドがある場合はサブコマンドのList、無い場合は親コマンドのListを返す
                if (subCommands[args[0]] != null) {
                    subCommands[args[0]]!!.onTabComplete(sender, command, args[0], args.drop(1).toTypedArray())
                } else {
                    onTabComplete(sender, command, label, args.toList())
                }
            }
        }
    }

    fun registerSubCommands(vararg commands: SubCommandBase) {
        commands.forEach { command ->
            // コマンド名(+エイリアス名)をキーとしてサブコマンドを登録
            (command.aliases + command.name).distinct().forEach { key ->
                subCommands[key] = command
            }
        }
    }
}
