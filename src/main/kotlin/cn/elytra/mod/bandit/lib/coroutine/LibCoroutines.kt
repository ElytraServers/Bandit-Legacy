package cn.elytra.mod.bandit.lib.coroutine

import cn.elytra.mod.bandit.BanditMod
import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.PlayerEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.debug.DebugProbes
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager
import kotlin.coroutines.EmptyCoroutineContext

@EventBusSubscriber
object LibCoroutines {
    private val DEBUG = System.getProperty("DEBUG") == "true" || true

    private const val TICK_MS = 50

    private val log = LogManager.getLogger()

    private var server: MinecraftServer? = null
    private var serverCoroutineScope: CoroutineScope =
        // will be recreated when the server initialized
        CoroutineScope(EmptyCoroutineContext).also { it.cancel() }

    private var thread: Thread? = null

    private val playerAssociatedScope: MutableMap<String, CoroutineScope> = mutableMapOf()

    /**
     * Initialize the server instance or cancel current scope.
     */
    internal fun initServer(server: MinecraftServer?) {
        LibCoroutines.server = server
        if (server != null) {
            serverCoroutineScope = CoroutineScope(EmptyCoroutineContext)
            @OptIn(ExperimentalCoroutinesApi::class)
            DebugProbes.install()
            if (DEBUG) log.info("Created a new server CoroutineScope")
        } else {
            serverCoroutineScope.cancel()
            if (DEBUG) log.info("Canceled the current server CoroutineScope")
        }
    }

    /**
     * @return time to sleep after running the queue.
     */
    @JvmStatic
    internal fun onMainThreadAboutToSleep(timeToSleep: Long): Long {
        if (thread == null) thread = Thread.currentThread()

        val timeStart = System.currentTimeMillis()

        do {
            val task = ServerDispatcher.queueToExecute.poll() ?: break
            try {
                task.run()
            } catch (th: Throwable) {
                BanditMod.logger.warn("Exception occurred while executing task", th)
            }
        } while (System.currentTimeMillis() - timeStart < timeToSleep)

        val timeElapsed = System.currentTimeMillis() - timeStart
        if (DEBUG && timeElapsed > TICK_MS * 10) {
            log.warn("Executed the queued tasks for too long, {}ms elapsed", timeElapsed)
        }
        return timeToSleep - timeElapsed
    }

    fun isServerThread(): Boolean = thread == Thread.currentThread()

    fun getCurrentServerScope(): CoroutineScope = serverCoroutineScope

    fun getPlayerAssociatedScope(p: EntityPlayerMP): CoroutineScope {
        if (DEBUG) log.info("Created a scope for player {}", p.displayName)
        return playerAssociatedScope.getOrPut(p.uniqueID.toString()) {
            CoroutineScope(getCurrentServerScope().coroutineContext + SupervisorJob())
        }
    }

    @JvmStatic
    @SubscribeEvent
    fun onPlayerLeftGame(e: PlayerEvent.PlayerLoggedOutEvent) {
        if (DEBUG) log.info("Canceling the scope for player {} because of leaving the game", e.player.displayName)
        playerAssociatedScope[e.player.uniqueID.toString()]?.cancel("The player has left the game")
    }
}
