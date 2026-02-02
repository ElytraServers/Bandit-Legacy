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

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>): List<String>? {
        if (args.isEmpty()) return super.addTabCompletionOptions(sender, args)

        val argsList = args.toMutableList()

        return when (argsList.removeFirstOrNull()) {
            "executor-generator", "executor" -> {
                getListOfStringsMatchingLastWord(
                    args,
                    *ExecutorGeneratorRegistry.all().map { (_, gen) ->
                        gen.getUnlocalizedName().substringAfterLast('.')
                    }.toTypedArray()
                )
            }

            "block-filter", "filter" -> {
                getListOfStringsMatchingLastWord(
                    args,
                    *BlockFilterRegistry.all().map { (_, filter) ->
                        filter.getUnlocalizedName().substringAfterLast('.')
                    }.toTypedArray()
                )
            }

            "drop_pos" -> {
                getListOfStringsMatchingLastWord(
                    args,
                    *getValidEnumValues<DropPosition>().toTypedArray()
                )
            }

            "drop_timing" -> {
                getListOfStringsMatchingLastWord(
                    args,
                    *getValidEnumValues<DropTiming>().toTypedArray()
                )
            }

            "stop_on_release" -> {
                getListOfStringsMatchingLastWord(
                    args,
                    "true",
                    "false"
                )
            }

            "stop ", "help" -> emptyList()

            else -> {
                getListOfStringsMatchingLastWord(
                    args,
                    "stop",
                    "help",
                    "drop_pos",
                    "drop_timing",
                    "stop_on_release",
                    "block-filter",
                    "executor-generator"
                ).takeIf { it.isNotEmpty() } ?: super.addTabCompletionOptions(sender, args)
            }
        }
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
