package cn.elytra.mod.bandit.mining.filter

import cn.elytra.mod.bandit.common.mining.VeinMiningContext
import cn.elytra.mod.bandit.common.util.HasUnlocalizedName
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import net.minecraft.world.World

interface VeinMiningBlockFilter : HasUnlocalizedName {
    val name: String

    /**
     * Filter the blocks to harvest. You can get [World] in [context], because the vein mining jobs
     * are not crossing dimensions.
     *
     * @return `true` to harvest the block at the given [pos].
     */
    fun isBlockMatching(
        context: VeinMiningContext,
        pos: BlockPos,
    ): Boolean

    override fun getUnlocalizedName(): String = "bandit.block-filter.$name"

    companion object {
        val ALL = DelegatedBlockFilter("all") { _, _ -> true }

        @Suppress("unused")
        val NONE = DelegatedBlockFilter("none") { _, _ -> false }

        val MATCH_BLOCK =
            DelegatedBlockFilter("match-block") { context, pos ->
                context.blockAndMeta.first == context.world.getBlock(pos.x, pos.y, pos.z)
            }

        val MATCH_BLOCK_AND_META =
            DelegatedBlockFilter("match-block-and-meta") { context, pos ->
                context.blockAndMeta.first == context.world.getBlock(pos.x, pos.y, pos.z) &&
                    context.blockAndMeta.second == context.world.getBlockMetadata(pos.x, pos.y, pos.z)
            }
    }
}
