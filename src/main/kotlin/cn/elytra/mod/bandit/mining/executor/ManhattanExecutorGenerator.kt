package cn.elytra.mod.bandit.mining.executor

import cn.elytra.mod.bandit.common.mining.VeinMiningContext
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import com.gtnewhorizon.gtnhlib.blockpos.IBlockPos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.minecraftforge.common.util.ForgeDirection
import java.util.*
import kotlin.math.pow

class ManhattanExecutorGenerator(
    override val name: String,
    maxManhattanRange: Int,
    val plusMode: Boolean = false,
) : SequencedVeinMiningExecutorGenerator() {
    private val maxManhattanRangeSquared = maxManhattanRange.toDouble().pow(2)

    class BlockPosComparator(
        val o: BlockPos,
    ) : Comparator<BlockPos> {
        override fun compare(
            o1: BlockPos,
            o2: BlockPos,
        ): Int = o.distanceSquared(o1) compareTo o.distanceSquared(o2)
    }

    override fun createSequence(context: VeinMiningContext): Flow<BlockPos> =
        flow {
            val queue =
                PriorityQueue(BlockPosComparator(context.center))
                    .also { it.add(context.center) }
            val visited = HashSet<BlockPos>()

            while (queue.isNotEmpty()) {
                // get the first block pos in the queue
                val p = queue.poll()
                if (p in visited) continue

                if (isBlockMatchingVein(context, p)) {
                    // emit the value
                    emit(p)
                    // put to the visited list
                    visited.add(p)
                    // find neighbors and add to the queue
                    val neighbors =
                        getNeighbors(p)
                            .filter { it !in visited }
                            .filter { context.center.distanceSq(it) <= maxManhattanRangeSquared }
                    queue.addAll(neighbors)
                }
            }
        }

    /**
     * From the center, get the neighbor blocks.
     * If [plusMode] is true, it yields all the blocks within 3x3x3 boundary except itself.
     * Otherwise, it yields all the blocks directly attached to the center block.
     */
    private fun getNeighbors(p: BlockPos): Sequence<BlockPos> =
        sequence {
            if (plusMode) {
                for (x in -1..1) {
                    for (y in -1..1) {
                        for (z in -1..1) {
                            if (x != 0 || y != 0 || z != 0) {
                                // skip the center
                                yield(BlockPos(p.x + x, p.y + y, p.z + z))
                            }
                        }
                    }
                }
            } else {
                yieldAll(
                    ForgeDirection.entries.map {
                        // FIX: GTNHLib changed the return type of BlockPos from "IBlockPos" to "BlockPos"
                        // in a later version, and this is ABI-incompatible.
                        // Thus, we cast p to IBlockPos, then cast the result back to BlockPos to fix this bug.
                        // TODO: Remove this weird cast line when we abandon GTNH 2.7.X.
                        (p as IBlockPos).offset(it) as BlockPos
                    },
                )
            }
        }
}

private fun BlockPos.distanceSq(other: BlockPos): Double {
    val dx = this.x + 0.5 - other.x
    val dy = this.y + 0.5 - other.y
    val dz = this.z + 0.5 - other.z
    return dx * dx + dy * dy + dz * dz
}
