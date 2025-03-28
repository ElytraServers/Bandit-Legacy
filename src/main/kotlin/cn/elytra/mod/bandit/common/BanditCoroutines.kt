package cn.elytra.mod.bandit.common

import cn.elytra.mod.bandit.BanditMod
import cn.elytra.mod.bandit.common.BanditCoroutines.callFromMainThread
import cn.elytra.mod.bandit.common.BanditCoroutines.initServer
import com.google.common.collect.Queues
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListenableFutureTask
import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.*
import net.minecraft.server.MinecraftServer
import net.minecraft.server.gui.IUpdatePlayerListBox
import java.util.Queue
import java.util.concurrent.*
import kotlin.coroutines.CoroutineContext

object BanditCoroutines {

    private val pool =
        ThreadPoolExecutor(
            4,
            6,
            1,
            TimeUnit.MINUTES,
            ArrayBlockingQueue(6),
            ThreadFactoryBuilder().setNameFormat("BanditThread-%d").setDaemon(true).build(),
            ThreadPoolExecutor.DiscardPolicy()
        )
            .asCoroutineDispatcher()

    /** The scope of VeinMining tasks. */
    var VeinMiningScope = CoroutineScope(pool)

    /** Dispatcher that runs the code on Minecraft Server Thread, to make sure the thread safety. */
    val ServerThreadDispatcher =
        object : CoroutineDispatcher() {
            override fun dispatch(context: CoroutineContext, block: Runnable) {
                if(isServerThread()) {
                    block.run()
                } else {
                    callFromMainThread(Executors.callable(block))
                }
            }
        }

    private val jobQueue: Queue<FutureTask<*>> = Queues.newArrayDeque()

    private var server: MinecraftServer? = null
    private var serverThread: Thread? = null

    /**
     * The [IUpdatePlayerListBox] (or Tickable) that will be added to the server, which will be called
     * on every tick.
     *
     * If you want to run a part of code on Server Thread, you may call [callFromMainThread]. If you
     * call it on Server Thread, it's immediately invoked, or it will be called on next server loop.
     *
     * @see initServer
     * @see callFromMainThread
     */
    val ticking = Runnable {
        synchronized(jobQueue) {
            while(jobQueue.isNotEmpty()) {
                val task = jobQueue.poll()
                try {
                    task.run()
                    task.get()
                } catch(e: ExecutionException) {
                    BanditMod.logger.error("Error executing task", e)
                } catch(e: InterruptedException) {
                    BanditMod.logger.error("Error executing task", e)
                }
            }
        }
        // set the server thread reference
        if(serverThread == null) {
            serverThread = Thread.currentThread()
        }
    }

    /**
     * @return `true` if the call site is on Server Thread; `false` if the server field is not
     * initialized.
     */
    fun isServerThread(): Boolean {
        return if(serverThread == null) false else Thread.currentThread() == serverThread
    }

    /**
     * @return `true` if the server is running; also `true` if the server field is not initialized.
     */
    private fun isServerNotStopped(): Boolean {
        return server?.isServerStopped != true
    }

    /** Initialize the required fields in this object. */
    internal fun initServer(s: MinecraftServer) {
        // add the ticking callback to the tick-able list
        server = s
        // refresh coroutine scope
        VeinMiningScope.cancel("refreshing context")
        VeinMiningScope = CoroutineScope(pool)
    }

    /**
     * Run the [callable] immediately if the call site is on Server Thread, or call it on the next
     * server loop.
     */
    @Suppress("UnstableApiUsage")
    internal fun <T> callFromMainThread(callable: Callable<T>): ListenableFuture<T> {
        if(!isServerThread() && isServerNotStopped()) {
            val listenableFutureTask = ListenableFutureTask.create(callable)

            synchronized(jobQueue) {
                jobQueue.add(listenableFutureTask)
                return listenableFutureTask
            }
        } else {
            return try {
                Futures.immediateFuture(callable.call())
            } catch(e: Exception) {
                Futures.immediateFailedCheckedFuture(e)
            }
        }
    }
}
