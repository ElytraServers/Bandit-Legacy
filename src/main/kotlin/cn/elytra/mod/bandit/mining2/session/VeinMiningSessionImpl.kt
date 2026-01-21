package cn.elytra.mod.bandit.mining2.session

import cn.elytra.mod.bandit.mining2.executor.Executor
import cn.elytra.mod.bandit.mining2.executor.ExecutorImpl
import cn.elytra.mod.bandit.mining2.matcher.BuiltinMatchers
import cn.elytra.mod.bandit.mining2.matcher.Matcher
import cn.elytra.mod.bandit.mining2.selector.BuiltinSelectors
import cn.elytra.mod.bandit.mining2.selector.Selector
import kotlinx.coroutines.CoroutineScope
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.world.WorldServer
import net.minecraftforge.common.util.BlockSnapshot

data class VeinMiningSessionImpl(
    override val coroutineScope: CoroutineScope,
    override val triggerSnapshot: BlockSnapshot,
    private val playerUniqueId: String,
    private val worldDimId: Int,
) : VeinMiningSession {
    constructor(
        coroutineScope: CoroutineScope,
        player: EntityPlayerMP,
        world: WorldServer,
        triggerSnapshot: BlockSnapshot,
    ) : this(coroutineScope, triggerSnapshot, player.uniqueID.toString(), world.provider.dimensionId)

    override val executor: Executor = ExecutorImpl(this)

    override val player: EntityPlayerMP
        get() =
            MinecraftServer
                .getServer()
                .configurationManager
                .playerEntityList
                .firstOrNull { it.uniqueID.toString() == playerUniqueId }
                ?: error("the player can't be found in the server, is he offline?")

    override val world: WorldServer
        get() =
            MinecraftServer
                .getServer()
                .worldServers
                .firstOrNull { it.provider.dimensionId == worldDimId }
                ?: error("the world can't be found in the server, is it broken?")

    override val matcher: Matcher
        get() = BuiltinMatchers.SameBlock

    override val selector: Selector
        get() = BuiltinSelectors.Default
}
