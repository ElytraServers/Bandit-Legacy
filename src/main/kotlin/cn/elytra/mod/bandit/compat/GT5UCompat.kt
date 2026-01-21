package cn.elytra.mod.bandit.compat

import cn.elytra.mod.bandit.BanditMod
import cn.elytra.mod.bandit.mining2.event.VeinMiningMatchEvent
import cn.elytra.mod.bandit.mining2.matcher.BuiltinMatchers
import cn.elytra.mod.bandit.mining2.session.getBlockTileEntity
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import gregtech.common.blocks.TileEntityOres
import net.minecraft.tileentity.TileEntity

internal object GT5UCompat {
    fun init() {
        BanditMod.bus.register(GT5UCompat)
    }

    private fun isGTOreMatching(
        a: TileEntity?,
        b: TileEntity?,
    ): Boolean = a is TileEntityOres && b is TileEntityOres && a.mMetaData == b.mMetaData

    @SubscribeEvent
    fun onBlockMatchingV2(e: VeinMiningMatchEvent) {
        val session = e.session
        if (session.matcher != BuiltinMatchers.SameBlockAndMetadata) return
        e.isBlockMatching =
            isGTOreMatching(session.triggerSnapshot.tileEntity, session.getBlockTileEntity<TileEntityOres>(e.pos))
    }
}
