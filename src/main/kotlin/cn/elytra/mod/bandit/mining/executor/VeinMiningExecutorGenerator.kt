package cn.elytra.mod.bandit.mining.executor

import cn.elytra.mod.bandit.common.mining.VeinMiningContext
import cn.elytra.mod.bandit.common.util.HasUnlocalizedName
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraft.util.Vec3
import net.minecraft.world.World

interface VeinMiningExecutorGenerator : HasUnlocalizedName {
    val name: String

    /**
     * From the given [context], generate a suspend function to execute the requested vein mining
     * task.
     */
    fun generate(context: VeinMiningContext): suspend () -> Unit

    override fun getUnlocalizedName(): String = "bandit.executor.$name"

    companion object {
        /** Spawn an [EntityItem] in the given [world] at given [pos] without random offset. */
        fun spawnItemAsEntity(
            world: World,
            pos: Vec3,
            item: ItemStack,
        ) {
            val entityItem = EntityItem(world, pos.xCoord, pos.yCoord, pos.zCoord, item)
            entityItem.delayBeforeCanPickup = 1
            world.spawnEntityInWorld(entityItem)
        }
    }
}
