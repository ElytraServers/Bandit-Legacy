package cn.elytra.mod.bandit.mining2.executor

import cn.elytra.mod.bandit.BanditMod
import cn.elytra.mod.bandit.common.Server
import cn.elytra.mod.bandit.mining2.matcher.Matcher
import cn.elytra.mod.bandit.mining2.matcher.matchesAndPostEvent
import cn.elytra.mod.bandit.mining2.selector.Selector
import cn.elytra.mod.bandit.mining2.session.VeinMiningSession
import cn.elytra.mod.bandit.network.BanditNetwork
import cn.elytra.mod.bandit.util.HarvestCollector
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.player.EntityPlayerMP
import kotlin.time.Duration.Companion.seconds

class ExecutorImpl(
    val session: VeinMiningSession,
) : Executor {
    private val chan = Channel<BlockPos>(Channel.UNLIMITED)

    private val _foundPositions = MutableStateFlow<List<BlockPos>>(emptyList())
    override val foundPositions: StateFlow<List<BlockPos>> get() = _foundPositions

    private var jobFindPos: Job? = null
    private var jobVeinMining: Job? = null

    private val player: EntityPlayerMP get() = session.player
    private val matcher: Matcher get() = session.matcher
    private val selector: Selector get() = session.selector

    override fun startFindPositions() {
        check(session.isValid) { "Session is invalid" }
        check(jobFindPos == null) { "Find-pos job has already been started" }

        jobFindPos =
            session.coroutineScope.launch {
                // collect all selected pos to channel
                selector
                    .select(session, matcher)
                    .collect { blockPos ->
                        chan.send(blockPos)
                        _foundPositions.update { it + blockPos }
                    }
            }

        // sync selected blocks
        @OptIn(FlowPreview::class)
        session.coroutineScope.launch {
            foundPositions.debounce(5.seconds).collect {
                BanditNetwork.syncBlockCacheToClient(session.player, it)
            }
        }
    }

    override fun startVeinMining() {
        check(session.isValid) { "Session is invalid" }
        check(jobVeinMining == null) { "Vein-mining job has already been started" }

        jobVeinMining =
            session.coroutineScope.launch {
                for (bp in chan) {
                    // check again if the block is valid, because it can be possibly changed
                    if (matcher.matchesAndPostEvent(session, bp)) {
                        launch(Dispatchers.Server) {
                            BanditMod.logger.debug(
                                "Harvesting at {} dim {} on behalf of {}",
                                bp,
                                session.world.provider.dimensionId,
                                session.player.displayName,
                            )

                            if (player.worldObj != session.world) {
                                BanditMod.logger.debug(
                                    "Skipped harvesting because the player wasn't in the world {} that starts the session {}",
                                    player.worldObj.provider.dimensionId,
                                    session.world.provider.dimensionId,
                                )
                                return@launch
                            }

                            val (drop, xp) =
                                HarvestCollector.withHarvestCollectorScope {
                                    player.theItemInWorldManager.tryHarvestBlock(bp.x, bp.y, bp.z)
                                }

                            // drop immediately for now
                            drop.forEach { item ->
                                val i = EntityItem(session.world, player.posX, player.posY, player.posZ, item)
                                session.world.spawnEntityInWorld(i)
                            }
                            if (xp > 0) {
                                val x = EntityXPOrb(session.world, player.posX, player.posY, player.posZ, xp)
                                session.world.spawnEntityInWorld(x)
                            }
                        }
                    }
                }
            }
    }

    override fun stopAll() {
        session.coroutineScope.cancel("Stop all")
    }

    override fun isRunning(): Boolean = jobVeinMining?.isActive == true

    override fun invokeOnComplete(action: (Throwable?) -> Unit) {
        jobVeinMining?.invokeOnCompletion(action)
    }
}
