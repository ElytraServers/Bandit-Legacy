package cn.elytra.mod.bandit.mining2.selector.helper

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import net.minecraftforge.common.util.ForgeDirection
import org.joml.component1
import org.joml.component2
import org.joml.component3

object BlockPosIteratorUtils {
    private val nearbyRange = -1..1

    fun getConnectedBlockPositions(
        center: BlockPos,
        allowCornerTouched: Boolean = false,
    ): Sequence<BlockPos> = if (allowCornerTouched) getCubicTouched(center) else getFaceToFaceTouched(center)

    fun getFaceToFaceTouched(center: BlockPos): Sequence<BlockPos> =
        sequence {
            ForgeDirection.VALID_DIRECTIONS.forEach { d ->
                yield(BlockPos(center.x + d.offsetX, center.y + d.offsetY, center.z + d.offsetZ))
            }
        }

    fun getCubicTouched(center: BlockPos): Sequence<BlockPos> =
        sequence {
            for (dx in nearbyRange) {
                for (dy in nearbyRange) {
                    for (dz in nearbyRange) {
                        // skip myself
                        if (dx == 0 && dy == 0 && dz == 0) continue
                        yield(BlockPos(center.x + dx, center.y + dy, center.z + dz))
                    }
                }
            }
        }

    fun getLargeScan(
        center: BlockPos,
        radiusXZ: Int,
        radiusY: Int,
    ): Sequence<BlockPos> =
        sequence {
            val (x, y, z) = center
            for (x in x - radiusXZ until x + radiusXZ) {
                for (z in z - radiusXZ until z + radiusXZ) {
                    // y iteration reversed from up to down
                    for (y in y + radiusY downTo y - radiusY) {
                        yield(BlockPos(x, y, z))
                    }
                }
            }
        }
}
