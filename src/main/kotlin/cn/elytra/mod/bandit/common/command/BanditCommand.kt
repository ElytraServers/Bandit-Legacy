package cn.elytra.mod.bandit.common.command

import cn.elytra.mod.bandit.common.mining.VeinMiningContext.DropPosition
import cn.elytra.mod.bandit.common.mining.VeinMiningContext.DropTiming
import cn.elytra.mod.bandit.common.player_data.veinMiningData
import cn.elytra.mod.bandit.common.util.parseValueToEnum
import cn.elytra.mod.bandit.mining.BlockFilterRegistry
import cn.elytra.mod.bandit.mining.ExecutorGeneratorRegistry
import cn.elytra.mod.bandit.mining.exception.CommandCancellation
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatComponentTranslation

object BanditCommand : CommandBase() {
    override fun getCommandName(): String = "bandit"
    override fun getCommandUsage(sender: ICommandSender?): String = "command.bandit.usage"

    override fun getRequiredPermissionLevel(): Int = 0

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean {
        return true
    }

    override fun processCommand(
        sender: ICommandSender,
        argsArray: Array<String>,
    ) {
        val args = argsArray.toMutableList()

        when(args.removeFirstOrNull()) {
            "executor", "executor-generator" -> handleExecutorSettings(sender, args)
            "filter", "block-filter" -> handleBlockFilterSettings(sender, args)
            "stop", "halt" -> handleHaltRequest(sender)
            "drop_pos" -> handleDropPos(sender, args)
            "drop_timing" -> handleDropTiming(sender, args)
            "stop_on_release" -> handleStopOnRelease(sender, args)
            else -> handleHelp(sender)
        }
    }

    class CommandNode(val name: String) {
        private val _sub = mutableListOf<CommandNode>()
        val sub: List<CommandNode> get() = _sub

        fun sub(vararg nodesOrNames: Any): CommandNode {
            nodesOrNames.forEach {
                when (it) {
                    is CommandNode -> _sub.add(it)
                    is String -> _sub.add(CommandNode(it))
                    is Iterable<*> -> it.forEach { n -> if (n is String) _sub.add(CommandNode(n)) }
                }
            }
            return this
        }

        fun complete(args: List<String>): List<String> {
            if (args.isEmpty()) return listOf(name)

            val head = args.first()
            val tail = args.drop(1)

            return when {
                !name.startsWith(head) -> emptyList()
                tail.isEmpty() -> listOf(name)
                name == head -> sub.flatMap { it.complete(tail) }
                else -> emptyList()
            }
        }
    }


    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>): List<String> {
        val commandTree = listOf(
//            Example:
//            CommandNode("complex").sub(
//                CommandNode("subLevel").sub(
//                        CommandNode("subLevel2a").sub("foo", "bar"),
//                        CommandNode("subLevel2b").sub(stringListFun()),
//                        CommandNode("subLevel2c")
//                ),
//            ),
            CommandNode("stop"),
            CommandNode("help"),

            CommandNode("stop_on_release").sub("true", "false"),
            CommandNode("drop_pos").sub(getValidEnumValues<DropPosition>()),
            CommandNode("drop_timing").sub(getValidEnumValues<DropTiming>()),

            CommandNode("executor-generator").sub(
                ExecutorGeneratorRegistry.all().values.map {
                    it.getUnlocalizedName().substringAfterLast('.')
                }
            ),
            CommandNode("block-filter").sub(
                BlockFilterRegistry.all().values.map {
                    it.getUnlocalizedName().substringAfterLast('.')
                }
            )
        )

        return commandTree.flatMap { it.complete(args.toList()) }
    }

    private fun ICommandSender.withEntityPlayer(block: (EntityPlayerMP) -> Unit) {
        if(this is EntityPlayerMP) {
            block(this)
        } else {
            addChatMessage(ChatComponentText("This command sender type is not supported!"))
        }
    }

    private fun handleExecutorSettings(
        sender: ICommandSender,
        strings: MutableList<String>,
    ) {
        sender.withEntityPlayer { p ->
            val raw = strings.removeFirstOrNull()
            if(raw == null) {
                val execId = p.veinMiningData.veinMiningExecutorId
                sender.addChatMessage(
                    ChatComponentTranslation(
                        "command.bandit.executor.current",
                        ExecutorGeneratorRegistry.get(execId)?.toChatComponent()
                    )
                )
                sender.addChatMessage(ChatComponentTranslation("command.bandit.executor.list"))
                ExecutorGeneratorRegistry.all().forEach { (id, _) ->
                    sender.addChatMessage(
                        ChatComponentTranslation(
                            "command.bandit.executor.list.entry",
                            id,
                            ExecutorGeneratorRegistry.get(id)?.toChatComponent()
                        )
                    )
                }
            } else {
                val executorId = ExecutorGeneratorRegistry.resolveExecutorId(raw)

                if (executorId == null) {
                    sender.addChatMessage(ChatComponentTranslation("command.bandit.executor.set.fail"))
                    return@withEntityPlayer
                }

                p.veinMiningData.veinMiningExecutorId = executorId
                sender.addChatMessage(
                    ChatComponentTranslation(
                        "command.bandit.executor.set.ok",
                        ExecutorGeneratorRegistry.get(executorId)?.toChatComponent()
                    )
                )
            }
        }
    }

    private fun handleBlockFilterSettings(
        sender: ICommandSender,
        strings: MutableList<String>,
    ) {
        sender.withEntityPlayer { p ->
            val raw = strings.removeFirstOrNull()
            if(raw == null) {
                val filterId = p.veinMiningData.veinMiningBlockFilterId
                sender.addChatMessage(
                    ChatComponentTranslation(
                        "command.bandit.filter.current",
                        BlockFilterRegistry.get(filterId)?.toChatComponent()
                    )
                )
                sender.addChatMessage(ChatComponentTranslation("command.bandit.filter.list"))
                BlockFilterRegistry.all().forEach { (id, _) ->
                    sender.addChatMessage(
                        ChatComponentTranslation(
                            "command.bandit.filter.list.entry",
                            id,
                            BlockFilterRegistry.get(id)?.toChatComponent()
                        )
                    )
                }
            } else {
                val filterId = BlockFilterRegistry.resolveFilterId(raw)

                if (filterId == null) {
                    sender.addChatMessage(ChatComponentTranslation("command.bandit.filter.set.fail"))
                    return@withEntityPlayer
                }

                p.veinMiningData.veinMiningBlockFilterId = filterId
                sender.addChatMessage(
                    ChatComponentTranslation(
                        "command.bandit.filter.set.ok",
                        BlockFilterRegistry.get(filterId)?.toChatComponent()
                    )
                )
            }
        }
    }

    private fun handleHelp(sender: ICommandSender) {
        for(i in 0..3) {
            sender.addChatMessage(ChatComponentTranslation("command.bandit.help.$i"))
        }
    }

    private fun handleHaltRequest(sender: ICommandSender) {
        sender.withEntityPlayer { p ->
            p.veinMiningData.cancelJob(CommandCancellation())
        }
    }

    private inline fun <reified T : Enum<T>> getValidEnumValues(): List<String> {
        return enumValues<T>().map { it.name.lowercase() }
    }

    private fun handleDropPos(
        sender: ICommandSender,
        args: MutableList<String>,
    ) {
        sender.withEntityPlayer { p ->
            if(args.isEmpty()) {
                sender.addChatMessage(
                    ChatComponentTranslation(
                        "command.bandit.drop_pos",
                        p.veinMiningData.harvestedDropPosition.toChatComponent(),
                        getValidEnumValues<DropPosition>().joinToString()
                    )
                )
            } else {
                when(val value = parseValueToEnum<DropPosition>(args[0])) {
                    null -> {
                        p.addChatMessage(
                            ChatComponentTranslation(
                                "command.bandit.drop_pos.invalid_argument",
                                args[0],
                                getValidEnumValues<DropPosition>().joinToString()
                            )
                        )
                    }

                    else -> {
                        p.veinMiningData.harvestedDropPosition = value
                        p.addChatMessage(ChatComponentTranslation("command.bandit.drop_pos.ok", value))
                    }
                }
            }
        }
    }

    private fun handleDropTiming(sender: ICommandSender, args: MutableList<String>) {
        sender.withEntityPlayer { p ->
            if(args.isEmpty()) {
                sender.addChatMessage(
                    ChatComponentTranslation(
                        "command.bandit.drop_timing",
                        p.veinMiningData.harvestedDropTiming.toChatComponent(),
                        getValidEnumValues<DropTiming>().joinToString()
                    )
                )
            } else {
                when(val value = parseValueToEnum<DropTiming>(args[0])) {
                    null -> {
                        p.addChatMessage(
                            ChatComponentTranslation(
                                "command.bandit.drop_timing.invalid_argument",
                                args[0],
                                getValidEnumValues<DropTiming>().joinToString()
                            )
                        )
                    }

                    else -> {
                        p.veinMiningData.harvestedDropTiming = value
                        p.addChatMessage(ChatComponentTranslation("command.bandit.drop_timing.ok", value))
                    }
                }
            }
        }
    }

    private fun handleStopOnRelease(sender: ICommandSender, args: MutableList<String>) {
        sender.withEntityPlayer { p ->
            if(args.isEmpty()) {
                p.addChatMessage(
                    ChatComponentTranslation(
                        "command.bandit.stop_on_release",
                        p.veinMiningData.stopVeinMiningOnKeyRelease
                    )
                )
            } else {
                when(val value = args[0].toBooleanStrictOrNull()) {
                    null -> {
                        p.addChatMessage(
                            ChatComponentTranslation(
                                "command.bandit.stop_on_release.invalid_argument",
                                args[0],
                                "[true, false]"
                            )
                        )
                    }

                    else -> {
                        p.veinMiningData.stopVeinMiningOnKeyRelease = value
                        p.addChatMessage(ChatComponentTranslation("command.bandit.stop_on_release.ok", value))
                    }
                }
            }
        }
    }
}
