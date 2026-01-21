package cn.elytra.mod.bandit.mining2.matcher

import cn.elytra.mod.bandit.mining2.session.VeinMiningSession
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import net.minecraft.block.Block
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.BlockSnapshot

object BuiltinMatchers {
    object All : Matcher {
        override suspend fun matches(
            session: VeinMiningSession,
            source: BlockSnapshot,
            block: Block,
            blockMetadata: Int,
            blockTileEntity: TileEntity?,
            blockPos: BlockPos,
        ): Boolean = true
    }

    object SameBlock : Matcher {
        override suspend fun matches(
            session: VeinMiningSession,
            source: BlockSnapshot,
            block: Block,
            blockMetadata: Int,
            blockTileEntity: TileEntity?,
            blockPos: BlockPos,
        ): Boolean = block == source.getReplacedBlock()
    }

    object SameBlockAndMetadata : Matcher {
        override suspend fun matches(
            session: VeinMiningSession,
            source: BlockSnapshot,
            block: Block,
            blockMetadata: Int,
            blockTileEntity: TileEntity?,
            blockPos: BlockPos,
        ): Boolean = block == source.getReplacedBlock() && blockMetadata == source.meta
    }
}
