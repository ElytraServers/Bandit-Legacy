package cn.elytra.mod.bandit.common.command

import cn.elytra.mod.bandit.common.mining.VeinMiningContext.DropPosition
import cn.elytra.mod.bandit.common.mining.VeinMiningContext.DropTiming
import cn.elytra.mod.bandit.common.player_data.veinMiningData
import cn.elytra.mod.bandit.common.util.parseValueToEnum
import cn.elytra.mod.bandit.mining.BlockFilterRegistry
import cn.elytra.mod.bandit.mining.BlockFilterRegistry.id
import cn.elytra.mod.bandit.mining.ExecutorGeneratorRegistry
import cn.elytra.mod.bandit.mining.ExecutorGeneratorRegistry.id
import cn.elytra.mod.bandit.mining.exception.CommandCancellation
import com.github.taskeren.brigadier_kt.*
import com.gtnewhorizon.gtnhlib.brigadier.BrigadierApi
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatComponentTranslation

object BanditCommands {
    private fun CommandContext<ICommandSender>.getPlayerSourceOrNull(): EntityPlayer? =
        when (val source = source) {
            is EntityPlayer -> {
                source
            }

            else -> {
                source.addChatMessage(ChatComponentText("This command sender type is not supported!"))
                null
            }
        }

    private inline fun <reified T : Enum<T>> getValidEnumValues(): List<String> = enumValues<T>().map { it.name.lowercase() }

    fun registerBanditCommand() =
        BrigadierApi.getCommandDispatcher().registerCommand("bandit") {
            literal("stop") {
                executesUnit {
                    val p = it.getPlayerSourceOrNull() ?: return@executesUnit
                    p.veinMiningData.cancelJob(CommandCancellation())
                }
            }

            literal("executor") {
                executesUnit { context ->
                    val p = context.getPlayerSourceOrNull() ?: return@executesUnit
                    // show the current executor
                    val execId = p.veinMiningData.veinMiningExecutorId
                    context.source.addChatMessage(
                        ChatComponentTranslation(
                            "command.bandit.executor.current",
                            ChatComponentTranslation("bandit.executor.$execId"),
                        ),
                    )
                    // show the list of executors
                    p.addChatMessage(ChatComponentTranslation("command.bandit.executor.list"))
                    ExecutorGeneratorRegistry.all().forEach { (id, generator) ->
                        p.addChatMessage(
                            ChatComponentTranslation(
                                "command.bandit.executor.list.entry",
                                id,
                                generator.toChatComponent(),
                            ),
                        )
                    }
                }

                argument("id", StringArgumentType.string()) {
                    suggestsBlocking { _, builder ->
                        ExecutorGeneratorRegistry.all().forEach { (_, generator) ->
                            builder.suggest(generator.name)
                        }
                    }
                    executesUnit { context ->
                        val p = context.getPlayerSourceOrNull() ?: return@executesUnit
                        val id: String by context
                        val exec = ExecutorGeneratorRegistry.get(id)
                        if (exec != null) {
                            p.veinMiningData.veinMiningExecutorId = exec.id
                            p.addChatMessage(ChatComponentTranslation("command.bandit.executor.set.ok"))
                        } else {
                            p.addChatMessage(ChatComponentTranslation("command.bandit.executor.set.fail"))
                        }
                    }
                }
            }

            literal("filter") {
                executesUnit { context ->
                    val p = context.getPlayerSourceOrNull() ?: return@executesUnit
                    // show the current filter
                    val filterId = p.veinMiningData.veinMiningBlockFilterId
                    p.addChatMessage(
                        ChatComponentTranslation(
                            "command.bandit.filter.current",
                            ChatComponentTranslation("bandit.filter.$filterId"),
                        ),
                    )
                    // show the list of filters
                    p.addChatMessage(ChatComponentTranslation("command.bandit.filter.list"))
                    BlockFilterRegistry.all().forEach { (id, filter) ->
                        p.addChatMessage(
                            ChatComponentTranslation(
                                "command.bandit.filter.list.entry",
                                id,
                                filter.toChatComponent(),
                            ),
                        )
                    }
                }

                argument("id", StringArgumentType.string()) {
                    suggestsBlocking { _, builder ->
                        BlockFilterRegistry.all().forEach { (_, filter) ->
                            builder.suggest(filter.name)
                        }
                    }

                    executesUnit { context ->
                        val p = context.getPlayerSourceOrNull() ?: return@executesUnit
                        val id: String by context
                        val filter = BlockFilterRegistry.get(id)
                        if (filter != null) {
                            p.veinMiningData.veinMiningBlockFilterId = filter.id
                            p.addChatMessage(ChatComponentTranslation("command.bandit.filter.set.ok"))
                        } else {
                            p.addChatMessage(ChatComponentTranslation("command.bandit.filter.set.fail"))
                        }
                    }
                }
            }

            literal("drop_pos") {
                executesUnit { context ->
                    val p = context.getPlayerSourceOrNull() ?: return@executesUnit
                    p.addChatMessage(
                        ChatComponentTranslation(
                            "command.bandit.drop_pos",
                            p.veinMiningData.harvestedDropPosition.toChatComponent(),
                            getValidEnumValues<DropPosition>().joinToString(),
                        ),
                    )
                }

                argument("value", StringArgumentType.string()) {
                    suggestsBlocking { _, builder ->
                        getValidEnumValues<DropPosition>().forEach { value -> builder.suggest(value) }
                    }

                    executesUnit { context ->
                        val p = context.getPlayerSourceOrNull() ?: return@executesUnit
                        val value: String by context
                        val enumValue = parseValueToEnum<DropPosition>(value)
                        if (enumValue != null) {
                            p.veinMiningData.harvestedDropPosition = enumValue
                            p.addChatMessage(ChatComponentTranslation("command.bandit.drop_pos.ok", value))
                        } else {
                            p.addChatMessage(
                                ChatComponentTranslation(
                                    "command.bandit.drop_pos.invalid_argument",
                                    value,
                                    getValidEnumValues<DropPosition>().joinToString(),
                                ),
                            )
                        }
                    }
                }
            }

            literal("drop_timing") {
                executesUnit { context ->
                    val p = context.getPlayerSourceOrNull() ?: return@executesUnit
                    p.addChatMessage(
                        ChatComponentTranslation(
                            "command.bandit.drop_timing",
                            p.veinMiningData.harvestedDropPosition.toChatComponent(),
                            getValidEnumValues<DropTiming>().joinToString(),
                        ),
                    )
                }

                argument("value", StringArgumentType.string()) {
                    suggestsBlocking { _, builder ->
                        getValidEnumValues<DropTiming>().forEach { value -> builder.suggest(value) }
                    }

                    executesUnit { context ->
                        val p = context.getPlayerSourceOrNull() ?: return@executesUnit
                        val value: String by context
                        val enumValue = parseValueToEnum<DropTiming>(value)
                        if (enumValue != null) {
                            p.veinMiningData.harvestedDropTiming = enumValue
                            p.addChatMessage(ChatComponentTranslation("command.bandit.drop_timing.ok", value))
                        } else {
                            p.addChatMessage(
                                ChatComponentTranslation(
                                    "command.bandit.drop_timing.invalid_argument",
                                    value,
                                    getValidEnumValues<DropTiming>().joinToString(),
                                ),
                            )
                        }
                    }
                }
            }

            literal("stop_on_release") {
                executesUnit { context ->
                    val p = context.getPlayerSourceOrNull() ?: return@executesUnit
                    p.addChatMessage(
                        ChatComponentTranslation(
                            "command.bandit.stop_on_release",
                            p.veinMiningData.stopVeinMiningOnKeyRelease,
                        ),
                    )
                }

                argument("value", BoolArgumentType.bool()) {
                    executesUnit { context ->
                        val p = context.getPlayerSourceOrNull() ?: return@executesUnit
                        val value: Boolean by context
                        p.veinMiningData.stopVeinMiningOnKeyRelease = value
                        p.addChatMessage(ChatComponentTranslation("command.bandit.stop_on_release.ok", value))
                    }
                }
            }
        }
}
