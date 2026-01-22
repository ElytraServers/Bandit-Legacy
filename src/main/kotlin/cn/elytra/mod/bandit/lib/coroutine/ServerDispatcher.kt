package cn.elytra.mod.bandit.lib.coroutine

import cn.elytra.mod.bandit.BanditMod
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext

object ServerDispatcher : CoroutineDispatcher() {
    internal val queueToExecute = ConcurrentLinkedQueue<Runnable>()

    override fun isDispatchNeeded(context: CoroutineContext): Boolean = !LibCoroutines.isServerThread()

    override fun dispatch(
        context: CoroutineContext,
        block: Runnable,
    ) {
        // TODO: REMOVE
        BanditMod.logger.info("Accepted a task, total {}", queueToExecute.size)
        queueToExecute += block
    }
}

@Suppress("UnusedReceiverParameter")
val Dispatchers.Server get() = ServerDispatcher
