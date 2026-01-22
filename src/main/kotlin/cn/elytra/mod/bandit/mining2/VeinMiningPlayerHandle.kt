package cn.elytra.mod.bandit.mining2

import codechicken.lib.raytracer.RayTracer
import cpw.mods.fml.common.gameevent.PlayerEvent
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.common.util.BlockSnapshot
import net.minecraftforge.common.util.FakePlayer

interface VeinMiningPlayerHandle {
    val player: EntityPlayerMP?

    // State

    /**
     * Whether the vein-mining activated or not
     */
    var activatedVeinMining: Boolean

    // VM

    /**
     * @return whether or not can update the context
     */
    fun canUpdateContext(): Boolean

    /**
     * Update the context
     *
     * The session may be changed according to the context
     *
     * @param snapshot the snapshot of the block of the session, `null` for cancel and remove the current session
     * @param interrupt if `true`, the current session is forcibly canceled and will be replaced with a brand-new session
     */
    fun updateContext(
        snapshot: BlockSnapshot?,
        interrupt: Boolean = false,
    )

    /**
     * Start finding block positions of the current session
     *
     * You should update context before calling this
     */
    fun startBlockFinding()

    /**
     * Start vein-mining of the current session
     *
     * You should update context before calling this
     */
    fun startVeinMining()

    /**
     * Cancel the current session
     */
    fun cancel(reason: String)

    companion object {
        private val handles: MutableMap<String, VeinMiningPlayerHandle> = mutableMapOf()

        fun get(p: EntityPlayer): VeinMiningPlayerHandle {
            check(p is EntityPlayerMP && p !is FakePlayer)
            val uuid = p.uniqueID.toString()
            return handles.getOrPut(uuid) { VeinMiningPlayerHandleImpl(uuid) }
        }

        internal fun removeByEvent(e: PlayerEvent.PlayerLoggedOutEvent) {
            handles.remove(e.player.uniqueID.toString())?.cancel("The owner is leaving the game")
        }
    }
}

/**
 * Update the context by the raytrace
 */
fun VeinMiningPlayerHandle.updateContextByPlayerRaytrace(p: EntityPlayer) {
    val mop = RayTracer.reTrace(p.worldObj, p)
    if (mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return
    updateContext(BlockSnapshot.getBlockSnapshot(p.worldObj, mop.blockX, mop.blockY, mop.blockZ))
}
