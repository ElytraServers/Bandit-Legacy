package cn.elytra.mod.bandit.common.command

import cn.elytra.mod.bandit.common.mining.VeinMiningContext.DropPosition
import cn.elytra.mod.bandit.common.mining.VeinMiningContext.DropTiming
import cn.elytra.mod.bandit.common.player_data.veinMiningData
import cn.elytra.mod.bandit.common.util.parseValueToEnum
import cn.elytra.mod.bandit.mining.BlockFilterRegistry
import cn.elytra.mod.bandit.mining.ExecutorGeneratorRegistry
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatComponentTranslation

object BanditCommand : CommandBase() {
    override fun getCommandName(): String? = "bandit"
    override fun getCommandUsage(sender: ICommandSender?): String? = "command.bandit.usage"

    override fun getRequiredPermissionLevel(): Int = 2

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
            else -> handleHelp(sender)
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
            val id = strings.removeFirstOrNull()
            if(id == null) {
                val execId = p.veinMiningData.veinMiningExecutorId
                sender.addChatMessage(
                    ChatComponentTranslation(
                        "command.bandit.executor.current",
                        ChatComponentTranslation("bandit.executor.$execId")
                    )
                )
                sender.addChatMessage(ChatComponentTranslation("command.bandit.executor.list"))
                ExecutorGeneratorRegistry.all().forEach { id, _ ->
                    sender.addChatMessage(
                        ChatComponentTranslation(
                            "command.bandit.executor.list.entry",
                            id,
                            ChatComponentTranslation("bandit.executor.$id")
                        )
                    )
                }
            } else {
                val id = parseInt(sender, id)
                val flag = true // TODO: validation
                p.veinMiningData.veinMiningExecutorId = id
                if(flag) sender.addChatMessage(ChatComponentTranslation("command.bandit.executor.set.ok"))
                else sender.addChatMessage(ChatComponentTranslation("command.bandit.executor.set.fail"))
            }
        }
    }

    private fun handleBlockFilterSettings(
        sender: ICommandSender,
        strings: MutableList<String>,
    ) {
        sender.withEntityPlayer { p ->
            val id = strings.removeFirstOrNull()
            if(id == null) {
                val filterId = p.veinMiningData.veinMiningBlockFilterId
                sender.addChatMessage(
                    ChatComponentTranslation(
                        "command.bandit.filter.current",
                        ChatComponentTranslation("bandit.filter.$filterId")
                    )
                )
                sender.addChatMessage(ChatComponentTranslation("command.bandit.filter.list"))
                BlockFilterRegistry.all().forEach { id, _ ->
                    sender.addChatMessage(
                        ChatComponentTranslation(
                            "command.bandit.filter.list.entry",
                            ChatComponentTranslation("bandit.filter.$filterId")
                        )
                    )
                }
            } else {
                val id = parseInt(sender, id)
                val flag = true // TODO: validation
                p.veinMiningData.veinMiningBlockFilterId = id
                if(flag) sender.addChatMessage(ChatComponentTranslation("command.bandit.filter.set.ok"))
                else sender.addChatMessage(ChatComponentTranslation("command.bandit.filter.set.fail"))
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
            p.veinMiningData.stopJob("stop command")
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

    @Suppress("RedundantOverride")
    override fun compareTo(other: Any?): Int {
        // I don't know why this is needed. Possibly some weird glitch from Unimined.
        return super.compareTo(other)
    }
}
