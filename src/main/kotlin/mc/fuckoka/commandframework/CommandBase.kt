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

    final override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        if (p3.isNotEmpty()) {
            val subCommand = subCommands[p3[0]]
            if (subCommand != null) {
                // サブコマンドの実行権限を持っていない場合は終了
                if (!subCommand.testPermission(p0)) return true

                // サブコマンドの実行結果をそのまま返す
                return subCommand.onCommand(p0, p1, p3[0], p3.drop(1).toTypedArray())
            }
        }

        return onCommand(p0, p1, p2, p3.toList())
    }

    final override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): MutableList<String>? {
        return when (p3.size) {
            1 -> {
                // 親コマンドの引数とサブコマンドを合わせたListを返す
                val joined = mutableListOf<String>()
                val parent = onTabComplete(p0, p1, p2, p3.toList()) ?: mutableListOf()
                val sub = subCommands.keys.filter { it.startsWith(p3[0]) }.toMutableList()
                joined.addAll(parent)
                joined.addAll(sub)
                joined
            }

            else -> {
                // サブコマンドがある場合はサブコマンドのList、無い場合は親コマンドのListを返す
                if (subCommands[p3[0]] != null) {
                    subCommands[p3[0]]!!.onTabComplete(p0, p1, p2, p3)
                } else {
                    onTabComplete(p0, p1, p2, p3.toList())
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
