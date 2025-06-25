package cn.elytra.mod.bandit.common.player_data

import cn.elytra.mod.bandit.BanditMod
import cn.elytra.mod.bandit.common.BanditCoroutines
import cn.elytra.mod.bandit.common.mining.VeinMiningContext
import cn.elytra.mod.bandit.common.mining.VeinMiningContext.DropPosition
import cn.elytra.mod.bandit.common.mining.VeinMiningContext.DropTiming
import cn.elytra.mod.bandit.common.mining.VeinMiningDataSave
import cn.elytra.mod.bandit.common.mining.VeinMiningHandler
import cn.elytra.mod.bandit.common.util.parseValueToEnum
import cn.elytra.mod.bandit.mining.BlockFilterRegistry
import cn.elytra.mod.bandit.mining.ExecutorGeneratorRegistry
import cn.elytra.mod.bandit.mining.executor.BlockPosCacheableExecutorGenerator
import cn.elytra.mod.bandit.mining.executor.VeinMiningExecutorGenerator
import cn.elytra.mod.bandit.mining.filter.VeinMiningBlockFilter
import cn.elytra.mod.bandit.network.BanditNetwork
import codechicken.lib.raytracer.RayTracer
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ChatComponentTranslation
import net.minecraft.util.MovingObjectPosition
import java.util.*
import kotlin.properties.Delegates

data class VeinMiningPlayerData(
    val uuid: String,
    var player: EntityPlayer,
) {

    private val playerMP get() = player as EntityPlayerMP

    var currentJob: TypedJob? by Delegates.observable(null) { _, oldValue, newValue ->
        if(oldValue != newValue) {
            // cancel ongoing job
            oldValue?.job?.takeIf { it.isActive }?.cancel()
            // make new job unregister itself when completed
            newValue?.job?.invokeOnCompletion { currentJob = null }
        }
    }

    /**
     * @return `true` if there is a job running. `false` if the job is `null` or has ended for any reason, including completed or canceled.
     */
    val hasJobRunning: Boolean get() = currentJob?.job?.isActive == true

    var veinMiningExecutorId: Int by Delegates.observable(0) { _, oldValue, newValue ->
        if(oldValue != newValue) {
            save()
            precalculatedVeinBlocks = null
        }
    }

    var veinMiningBlockFilterId: Int by Delegates.observable(0) { _, oldValue, newValue ->
        if(oldValue != newValue) {
            save()
            precalculatedVeinBlocks = null
        }
    }

    var veinMiningKeyPressed: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
        // if(oldValue != newValue) { }
    }

    var harvestedDropPosition: DropPosition by Delegates.observable(DropPosition.DROP_TO_PLAYER) { _, oldValue, newValue ->
        if(oldValue != newValue) {
            save()
        }
    }

    var harvestedDropTiming: DropTiming by Delegates.observable(DropTiming.ITEM_IMMEDIATELY_XP_EVENTUALLY) { _, oldValue, newValue ->
        if(oldValue != newValue) {
            save()
        }
    }

    /**
     * The hash of the context when the precalculate take place.
     * It is used to check if there is going to start another "SAME" precalculate.
     */
    internal var precalculatedHash: Int = -1

    /**
     * The cached block pos list with given context (Executor, Filter, Center Block Position and Metadata).
     */
    var precalculatedVeinBlocks: List<BlockPos>? by Delegates.observable(null) { _, oldValue, newValue ->
        if(oldValue != newValue) {
            if(newValue == null) {
                precalculatedHash = -1
                BanditNetwork.syncBlockCacheToClient(playerMP, null)
            } else {
                BanditNetwork.syncBlockCacheToClient(playerMP, newValue)
            }
        }
    }

    companion object {
        const val TAG_EXECUTOR_GENERATOR = "VeinExecutorGeneratorData"
        const val TAG_BLOCK_FILTER = "VeinBlockFilterData"
        const val TAG_PREFERRED_DROP_POS = "HarvestedDropPosition"
        const val TAG_PREFERRED_DROP_TIMING = "HarvestedDropTiming"

        internal val InstanceMap = mutableMapOf<String, VeinMiningPlayerData>()

        fun getOrCreate(player: EntityPlayer): VeinMiningPlayerData {
            val uuidStr = player.uniqueID.toString()

            val data = InstanceMap[uuidStr]
            return if(data != null) {
                // check if the player is same
                if(data.player != player) {
                    data.player = player
                }
                data
            } else {
                // create a new data
                val data = VeinMiningPlayerData(uuidStr, player)
                InstanceMap.put(uuidStr, data)
                data
            }
        }

        /**
         * Used to calculate the hash of cached vein blocks.
         */
        internal fun precalculatedHash(bp: BlockPos, block: Block, blockMeta: Int): Int {
            return Objects.hash(bp, block, blockMeta)
        }
    }

    init {
        load()
    }

    internal fun load() {
        val nbt = VeinMiningDataSave.get().getPlayerData(uuid)
        readFromNBT(nbt)
    }

    internal fun save() {
        val nbt = NBTTagCompound().apply { writeToNBT(this) }
        VeinMiningDataSave.get().setPlayerData(uuid, nbt)
        VeinMiningDataSave.get().markDirty()
    }

    /**
     * Read from "WorldSavedData".
     */
    internal fun readFromNBT(nbtTag: NBTTagCompound) = apply {
        veinMiningExecutorId = nbtTag.getInteger(TAG_EXECUTOR_GENERATOR)
        veinMiningBlockFilterId = nbtTag.getInteger(TAG_BLOCK_FILTER)
        harvestedDropPosition = parseValueToEnum(
            nbtTag.getString(TAG_PREFERRED_DROP_POS),
            DropPosition.DROP_TO_PLAYER
        )
        harvestedDropTiming = parseValueToEnum(
            nbtTag.getString(TAG_PREFERRED_DROP_TIMING),
            DropTiming.ITEM_IMMEDIATELY_XP_EVENTUALLY
        )
    }

    /**
     * Write to "WorldSavedData".
     */
    internal fun writeToNBT(nbtTag: NBTTagCompound) = apply {
        nbtTag.setInteger(TAG_EXECUTOR_GENERATOR, veinMiningExecutorId)
        nbtTag.setInteger(TAG_BLOCK_FILTER, veinMiningBlockFilterId)
        nbtTag.setString(TAG_PREFERRED_DROP_POS, harvestedDropPosition.name)
        nbtTag.setString(TAG_PREFERRED_DROP_TIMING, harvestedDropTiming.name)
    }

    fun getExecutorGenerator(): VeinMiningExecutorGenerator {
        return ExecutorGeneratorRegistry.getOrDefault(veinMiningExecutorId)
    }

    fun getBlockFilter(): VeinMiningBlockFilter {
        return BlockFilterRegistry.getOrDefault(veinMiningBlockFilterId)
    }

    /**
     * Precalculate the block pos list with current context.
     *
     * The executor generator must be [BlockPosCacheableExecutorGenerator].
     */
    fun precalculateVeinBlocks() {
        if(hasJobRunning) return

        val execGenerator = getExecutorGenerator()
        if(execGenerator !is BlockPosCacheableExecutorGenerator) return

        val world = player.worldObj
        val mop = RayTracer.reTrace(world, player)
        // invalid raytrace hit type
        if(mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return

        val pos = BlockPos(mop.blockX, mop.blockY, mop.blockZ)
        val block = world.getBlock(mop.blockX, mop.blockY, mop.blockZ)
        val blockMeta = world.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ)
        val blockAndMeta = block to blockMeta

        val hash = precalculatedHash(pos, block, blockMeta)
        if(precalculatedVeinBlocks != null) {
            if(hash == precalculatedHash) return // same cache hit
        }
        precalculatedHash = hash

        val executionId = VeinMiningHandler.executionCounter.andIncrement
        val context = VeinMiningContext(
            world = world,
            center = pos,
            blockAndMeta = blockAndMeta,
            player = playerMP,
            filter = getBlockFilter(),
            executionId = executionId,
        )

        BanditMod.logger.info("Start caching #${executionId}")
        val job = BanditCoroutines.VeinMiningScope.launch(start = CoroutineStart.LAZY) {
            val result = execGenerator.precalculateBlockPosList(context)
            precalculatedVeinBlocks = result
        }
        job.invokeOnCompletion {
            BanditMod.logger.info("Cached #${executionId}")
            if(it != null) BanditMod.logger.warn("Caused by an Throwable", it)
        }
        currentJob = TypedJob(job, TypedJob.JobType.VeinMiningPrecalculating)

        job.start()
    }

    fun startVeinMining(x: Int, y: Int, z: Int) {
        val currentJobNow = currentJob
        if(currentJobNow != null) {
            when(currentJobNow.type) {
                // stop precalculate job and continue to start vein mining
                TypedJob.JobType.VeinMiningPrecalculating -> currentJob = null
                // stop if there is another vein mining job
                TypedJob.JobType.VeinMining -> return
            }
        }

        val world = player.worldObj
        val pos = BlockPos(x, y, z)
        val block = world.getBlock(x, y, z)
        val blockMeta = world.getBlockMetadata(x, y, z)
        val blockAndMeta = block to blockMeta
        val executionId = VeinMiningHandler.executionCounter.andIncrement

        // check hash, in case of MOP traced to a wrong block.
        // if so, we need to recalculate the blocks now, and give up the cached ones.
        val hash = precalculatedHash(pos, block, blockMeta)
        val precalculatedVeinBlocksChecked = if(hash == precalculatedHash) precalculatedVeinBlocks else null

        val context = VeinMiningContext(
            world = world,
            center = pos,
            blockAndMeta = blockAndMeta,
            player = playerMP,
            filter = getBlockFilter(),
            executionId = executionId,
            precalculatedBlockPosList = precalculatedVeinBlocksChecked,
            harvestedDropPosition = harvestedDropPosition,
            harvestedDropTiming = harvestedDropTiming,
        )
        val executor = getExecutorGenerator().generate(context)

        BanditMod.logger.info("Executing Vein Mining #${executionId}")
        player.addChatMessage(ChatComponentTranslation("bandit.message.task-starting"))
        player.addChatMessage(ChatComponentTranslation("bandit.message.task-halt-hint"))

        val job = BanditCoroutines.VeinMiningScope.launch(start = CoroutineStart.LAZY) {
            world.playSoundEffect(player.posX, player.posY, player.posZ, "note.harp", 3.0F, 1.0F)
            executor()
        }
        job.invokeOnCompletion {
            BanditMod.logger.info("VeinMining #${executionId} has ended")
            if(it != null) BanditMod.logger.warn("Caused by an Throwable", it)
            player.addChatMessage(
                ChatComponentTranslation(
                    "bandit.message.task-done",
                    context.statBlocksMined.get(),
                    context.statItemDropped.values.sum()
                )
            )
            // play sound effects
            world.playSoundEffect(player.posX, player.posY, player.posZ, "note.harp", 3.0F, 3.5F)
            // clear caches
            precalculatedVeinBlocks = null
        }
        currentJob = TypedJob(job, TypedJob.JobType.VeinMining)

        job.start()
    }

    fun stopJob(reason: String = "no reason") {
        currentJob?.job?.cancel("stopping for $reason")
    }

    fun stopAndClear() {
        stopJob()
        veinMiningKeyPressed = false
        InstanceMap -= this.uuid
    }

}

internal val EntityPlayer.veinMiningData: VeinMiningPlayerData get() = VeinMiningPlayerData.getOrCreate(this)
