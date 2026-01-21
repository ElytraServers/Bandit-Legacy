package cn.elytra.mod.bandit.mining2

import cn.elytra.mod.bandit.mining2.session.VeinMiningSession
import cn.elytra.mod.bandit.mining2.session.VeinMiningSessionImpl
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.world.WorldServer
import net.minecraftforge.common.util.BlockSnapshot
import org.joml.component1
import org.joml.component2
import org.joml.component3

object MiningV2 {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName("MiningV2"))

    private val sessionMap: MutableMap<String, VeinMiningSession> = mutableMapOf()

    internal fun newSession(
        player: EntityPlayer,
        blockPos: BlockPos,
    ): VeinMiningSession {
        val uuid = player.uniqueID.toString()
        // drop old session if exists
        sessionMap[uuid]?.drop()

        val world = player.worldObj
        val (x, y, z) = blockPos

        val newSession =
            sessionMap.getOrPut(uuid) {
                VeinMiningSessionImpl(
                    coroutineScope,
                    player as EntityPlayerMP,
                    world as WorldServer,
                    BlockSnapshot.getBlockSnapshot(world, x, y, z),
                )
            }

        return newSession
    }

    internal fun getSession(player: EntityPlayer): VeinMiningSession? = sessionMap[player.uniqueID.toString()]

    internal fun dropAny(player: EntityPlayer) = getSession(player)?.drop()
}
