package cn.elytra.mod.bandit.common.mining

import cn.elytra.mod.bandit.common.util.HasUnlocalizedName
import cn.elytra.mod.bandit.common.util.newItemStackHashMap
import cn.elytra.mod.bandit.mining.filter.VeinMiningBlockFilter
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

data class VeinMiningContext(
    val world: World,
    val center: BlockPos,
    val blockAndMeta: Pair<Block, Int>,
    val blockTileEntity: TileEntity?,
    val player: EntityPlayerMP,
    val filter: VeinMiningBlockFilter,
    val executionId: Int = -1,
    val precalculatedBlockPosList: List<BlockPos>? = null,
    val veinMiningMaxCountLimit: Int = Int.MAX_VALUE,
    val veinMiningCountPerOperation: Int = 64,
    val harvestedDropPosition: DropPosition = DropPosition.DROP_TO_PLAYER,
    val harvestedDropTiming: DropTiming = DropTiming.ITEM_IMMEDIATELY_XP_EVENTUALLY,
    val statBlocksMined: AtomicInteger = AtomicInteger(0),
    val statItemDropped: MutableMap<ItemStack, Int> = newItemStackHashMap(),
    val statXpValueCollected: AtomicInteger = AtomicInteger(0),
    val startedAt: LocalDateTime = LocalDateTime.now(),
) {

    enum class DropPosition : HasUnlocalizedName {
        DROP_AT_START {
            override fun getUnlocalizedName(): String = "bandit.drop_pos.start"
        },
        DROP_TO_PLAYER {
            override fun getUnlocalizedName(): String = "bandit.drop_pos.player"
        },
        ;
    }

    enum class DropTiming : HasUnlocalizedName {
        /**
         * Drop the harvested objects when they are harvested.
         */
        IMMEDIATELY {
            override fun getUnlocalizedName(): String = "bandit.drop_timing.immediately"
        },

        /**
         * Drop the harvested items immediately and xp at the end of the vein mining.
         */
        ITEM_IMMEDIATELY_XP_EVENTUALLY {
            override fun getUnlocalizedName(): String = "bandit.drop_timing.item_immediately"
        },

        /**
         * Drop the harvested objects at the end of the vein mining.
         */
        EVENTUALLY {
            override fun getUnlocalizedName(): String = "bandit.drop_timing.eventually"
        },
    }

}
