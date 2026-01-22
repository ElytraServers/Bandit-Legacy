package cn.elytra.mod.bandit.mining2.session

import cn.elytra.mod.bandit.mining2.executor.Executor
import cn.elytra.mod.bandit.mining2.executor.ExecutorImpl
import cn.elytra.mod.bandit.mining2.matcher.BuiltinMatchers
import cn.elytra.mod.bandit.mining2.matcher.Matcher
import cn.elytra.mod.bandit.mining2.selector.BuiltinSelectors
import cn.elytra.mod.bandit.mining2.selector.Selector
import cn.elytra.mod.bandit.util.MinecraftUtils
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.world.WorldServer
import net.minecraftforge.common.util.BlockSnapshot

data class VeinMiningSessionImpl(
    override val coroutineScope: CoroutineScope,
    override val triggerSnapshot: BlockSnapshot,
    private val playerUniqueId: String,
    private val worldDimId: Int,
) : VeinMiningSession {
    constructor(
        parentScope: CoroutineScope,
        player: EntityPlayerMP,
        world: WorldServer,
        triggerSnapshot: BlockSnapshot,
    ) : this(
        CoroutineScope(parentScope.coroutineContext + SupervisorJob() + CoroutineName("VMSession") + Dispatchers.Unconfined),
        triggerSnapshot,
        player.uniqueID.toString(),
        world.provider.dimensionId,
    )

    override val executor: Executor = ExecutorImpl(this)

    override val player: EntityPlayerMP
        get() =
            MinecraftUtils.findPlayerByUniqueId(playerUniqueId)
                ?: error("the player can't be found in the server, is he offline?")

    override val world: WorldServer
        get() =
            MinecraftUtils.findWorldByDimId(worldDimId)
                ?: error("the world can't be found in the server, is it broken?")

    override val matcher: Matcher
        get() = BuiltinMatchers.SameBlock

    override val selector: Selector
        get() = BuiltinSelectors.Default
}
