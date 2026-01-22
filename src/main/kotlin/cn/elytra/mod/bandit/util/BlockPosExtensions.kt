package cn.elytra.mod.bandit.util

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.util.BlockSnapshot
import org.joml.component1
import org.joml.component2
import org.joml.component3

fun newBlockSnapshot(
    world: World,
    bp: BlockPos,
): BlockSnapshot {
    val (x, y, z) = bp
    return BlockSnapshot.getBlockSnapshot(world, x, y, z)
}
