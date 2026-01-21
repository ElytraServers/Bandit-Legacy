package cn.elytra.mod.bandit.mining2.session

import cn.elytra.mod.bandit.BanditMod
import cn.elytra.mod.bandit.mining2.executor.Executor
import cn.elytra.mod.bandit.mining2.matcher.Matcher
import cn.elytra.mod.bandit.mining2.selector.Selector
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.WorldServer
import net.minecraftforge.common.util.BlockSnapshot

interface VeinMiningSession {
    /**
     * The coroutine scope of this session
     */
    val coroutineScope: CoroutineScope

    /**
     * The player that the session associated with
     */
    val player: EntityPlayerMP

    /**
     * The world that the session associated with
     */
    val world: WorldServer

    /**
     * The block snapshot of the block that starts this session
     */
    val triggerSnapshot: BlockSnapshot

    /**
     * The block position that starts this session
     */
    val triggerBlockPos: BlockPos get() = BlockPos(triggerSnapshot.x, triggerSnapshot.y, triggerSnapshot.z)

    val matcher: Matcher

    val selector: Selector

    val executor: Executor

    /**
     * @return `true` if the session is valid, where the player and the world is valid, the coroutine scope is active.
     */
    val isValid: Boolean
        get() =
            try {
                player
                world
                coroutineScope.isActive
            } catch (_: Exception) {
                false
            }

    /**
     * Drop the session and release all resource, canceling jobs
     */
    fun drop() {
        coroutineScope.cancel("Session dropped")
    }

    fun startFindPositions() {
        try {
            executor.startFindPositions()
        } catch (e: Exception) {
            BanditMod.logger.error("Failed to start find positions", e)
        }
    }

    fun startVeinMining() {
        try {
            executor.startVeinMining()
        } catch (e: Exception) {
            BanditMod.logger.error("Failed to start vein mining", e)
        }
    }
}

fun VeinMiningSession.getBlockSnapshot(bp: BlockPos): BlockSnapshot = BlockSnapshot.getBlockSnapshot(world, bp.x, bp.y, bp.z)

fun VeinMiningSession.getBlockAndMetadata(bp: BlockPos): Pair<Block, Int> =
    world.getBlock(bp.x, bp.y, bp.z) to world.getBlockMetadata(bp.x, bp.y, bp.z)

@Suppress("UNCHECKED_CAST")
fun <T : TileEntity> VeinMiningSession.getBlockTileEntity(bp: BlockPos): T? = world.getTileEntity(bp.x, bp.y, bp.z) as? T

fun VeinMiningSession?.isRunning(): Boolean = this != null && this.isValid && this.executor.isRunning()
