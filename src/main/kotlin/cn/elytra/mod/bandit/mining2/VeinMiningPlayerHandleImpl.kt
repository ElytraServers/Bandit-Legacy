package cn.elytra.mod.bandit.mining2

import cn.elytra.mod.bandit.common.player_data.veinMiningData
import cn.elytra.mod.bandit.lib.coroutine.LibCoroutines
import cn.elytra.mod.bandit.mining2.session.VeinMiningSession
import cn.elytra.mod.bandit.mining2.session.VeinMiningSessionImpl
import cn.elytra.mod.bandit.util.MinecraftUtils
import kotlinx.coroutines.isActive
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.world.WorldServer
import net.minecraftforge.common.util.BlockSnapshot
import org.apache.logging.log4j.LogManager
import kotlin.properties.Delegates

class VeinMiningPlayerHandleImpl(
    private val uuid: String,
) : VeinMiningPlayerHandle {
    private var session: VeinMiningSession? by Delegates.observable(null) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            // cancel the old session if active
            if (oldValue != null && oldValue.coroutineScope.isActive) oldValue.cancel("Replaced by a new session")
        }
    }

    override val player: EntityPlayerMP? get() = MinecraftUtils.findPlayerByUniqueId(uuid)

    // TODO: move the field to this impl
    override var activatedVeinMining: Boolean
        get() = player!!.veinMiningData.veinMiningKeyPressed
        set(value) {
            player!!.veinMiningData.veinMiningKeyPressed = value
        }

    override fun canUpdateContext(): Boolean {
        // can update context if the session is missing
        val session = session ?: return true
        try {
            session.player
            session.world
        } catch (_: Exception) {
            // the required stuff is nowhere to find in the server.
            // their reference may change, so we retrieve them by their id,
            // and causing them to be nullable (e.g., the player has left the game).
            return false
        }
        return session.coroutineScope.isActive && !session.executor.isRunning()
    }

    override fun updateContext(
        snapshot: BlockSnapshot?,
        interrupt: Boolean,
    ) {
        if (!interrupt && !canUpdateContext()) return

        // set to null, so the session is immediately canceled
        if (interrupt) session = null

        val p = player ?: return
        val delegate = session

        // only update the session when the snapshot is changed
        // TODO: update the session when the user preference is changed
        if (snapshot == null) {
            this.session = null
        } else if (delegate == null || delegate.triggerSnapshot != snapshot) {
            this.session = newSession(p, snapshot)
        }
    }

    override fun startBlockFinding() {
        val delegate = session ?: return log.warn("Context is not initialized when invoking startBlockFinding")
        delegate.startFindPositions()
    }

    override fun startVeinMining() {
        val delegate = session ?: return log.warn("Context is not initialized when invoking startVeinMining")
        delegate.startVeinMining()
        // unset the session to allow new sessions
        delegate.invokeOnComplete { session = null }
    }

    override fun cancel(reason: String) {
        session?.cancel(reason)
        session = null
    }

    companion object {
        private val log = LogManager.getLogger()

        /**
         * Create a new session associated with the given inputs
         */
        private fun newSession(
            player: EntityPlayerMP,
            snapshot: BlockSnapshot,
        ): VeinMiningSession {
            val world = player.worldObj as WorldServer
            val parentScope = LibCoroutines.getPlayerAssociatedScope(player)
            return VeinMiningSessionImpl(parentScope, player, world, snapshot)
        }
    }
}
