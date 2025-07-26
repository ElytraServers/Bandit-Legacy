package cn.elytra.mod.bandit.compat

import cn.elytra.mod.bandit.BanditMod
import cn.elytra.mod.bandit.mining.event.VeinMiningEvent
import cn.elytra.mod.bandit.mining.filter.VeinMiningBlockFilter
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import gregtech.common.blocks.TileEntityOres

internal object GT5UCompat {

    fun init() {
        BanditMod.bus.register(GT5UCompat)
    }

    @SubscribeEvent
    fun onBlockMatching(e: VeinMiningEvent.BlockMatching) {
        // must match metadata
        if(e.context.filter != VeinMiningBlockFilter.MATCH_BLOCK_AND_META) return;

        val te = e.world.getTileEntity(e.pos.x, e.pos.y, e.pos.z)
        if(te is TileEntityOres) {
            val teCenter = e.context.blockTileEntity
            if(teCenter is TileEntityOres) {
                e.isBlockMatching = te.mMetaData == teCenter.mMetaData
            }
        }
    }

}
