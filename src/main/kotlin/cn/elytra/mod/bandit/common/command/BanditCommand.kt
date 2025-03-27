package cn.elytra.mod.bandit.common.command

import cn.elytra.mod.bandit.common.player_data.veinMiningData
import cn.elytra.mod.bandit.mining.BlockFilterRegistry
import cn.elytra.mod.bandit.mining.ExecutorGeneratorRegistry
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatComponentTranslation

object BanditCommand : CommandBase() {
    override fun getCommandName(): String? = "bandit"
    override fun getCommandUsage(sender: ICommandSender?): String? = "command.bandit.usage"

    override fun processCommand(
        sender: ICommandSender,
        argsArray: Array<String>,
    ) {
        val args = argsArray.toMutableList()

        when(args.removeFirstOrNull()) {
            "executor", "executor-generator" -> handleExecutorSettings(sender, args)
            "filter", "block-filter" -> handleBlockFilterSettings(sender, args)
            "stop", "halt" -> handleHaltRequest(sender)
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
        for(i in 0..1) {
            sender.addChatMessage(ChatComponentTranslation("command.bandit.help.$i"))
        }
    }

    private fun handleHaltRequest(sender: ICommandSender) {
        sender.withEntityPlayer { p ->
            p.veinMiningData.stopJob("stop command")
        }
    }
}
