package cn.elytra.mod.bandit.common.mining

import cn.elytra.mod.bandit.mining.filter.VeinMiningBlockFilter
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.world.World
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

data class VeinMiningContext(
    val world: World,
    val center: BlockPos,
    val blockAndMeta: Pair<Block, Int>,
    val player: EntityPlayerMP,
    val filter: VeinMiningBlockFilter,
    val executionId: Int = -1,
    val precalculatedBlockPosList: List<BlockPos>? = null,
    val veinMiningMaxCountLimit: Int = Int.MAX_VALUE,
    val veinMiningCountPerOperation: Int = 64,
    val statBlocksMined: AtomicInteger = AtomicInteger(0),
    val statItemsCollected: AtomicInteger = AtomicInteger(0),
    val statXpValueCollected: AtomicInteger = AtomicInteger(0),
    val startedAt: LocalDateTime = LocalDateTime.now(),
)
