package cn.elytra.mod.bandit.mining2.executor

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import kotlinx.coroutines.flow.StateFlow

interface Executor {
    /**
     * The up-to-date list of found block positions to be vein-mined
     *
     * It will remain empty before you call [startFindPositions].
     */
    val foundPositions: StateFlow<List<BlockPos>>

    fun startFindPositions()

    fun startVeinMining()

    /**
     * @return `true` if the _vein-mining_ job is currently running (active)
     */
    fun isRunning(): Boolean

    /**
     * Run [action] when _vein-mining_ job is completed (both success or failure)
     */
    fun invokeOnComplete(action: (Throwable?) -> Unit)
}
