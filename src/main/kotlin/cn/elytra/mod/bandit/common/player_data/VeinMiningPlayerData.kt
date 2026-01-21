package cn.elytra.mod.bandit.common.player_data

import cn.elytra.mod.bandit.common.mining.VeinMiningDataSave
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.server.MinecraftServer
import java.util.*
import kotlin.properties.Delegates

data class VeinMiningPlayerData(
    val uuid: String,
) {
    private fun getPlayer(): EntityPlayer = checkNotNull(getPlayerFromUUID(uuid)) { "Failed to find the player with UUID: $uuid" }

    var selectorIndex: Int by Delegates.observable(0) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            save()
        }
    }

    var matcherIndex: Int by Delegates.observable(0) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            save()
        }
    }

    var veinMiningKeyPressed: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            if (!newValue) { // key released
                // if (stopVeinMiningOnKeyRelease) cancelJob(KeyReleaseCancellation())
            }
        }
    }

    var stopVeinMiningOnKeyRelease: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            save()
        }
    }

    companion object {
        const val TAG_EXECUTOR_GENERATOR = "VeinExecutorGeneratorData"
        const val TAG_BLOCK_FILTER = "VeinBlockFilterData"
        const val TAG_PREFERRED_DROP_POS = "HarvestedDropPosition"
        const val TAG_PREFERRED_DROP_TIMING = "HarvestedDropTiming"
        const val TAG_STOP_VEIN_MINING_ON_KEY_RELEASE = "StopVeinMiningOnKeyRelease"

        internal val InstanceMap = mutableMapOf<String, VeinMiningPlayerData>()

        fun getOrCreate(player: EntityPlayer): VeinMiningPlayerData {
            val uuidStr = player.uniqueID.toString()
            return InstanceMap.computeIfAbsent(uuidStr) { VeinMiningPlayerData(uuidStr) }
        }

        /**
         * Used to calculate the hash of cached vein blocks.
         */
        internal fun precalculatedHash(
            bp: BlockPos,
            block: Block,
            blockMeta: Int,
        ): Int = Objects.hash(bp, block, blockMeta)

        fun getPlayerFromUUID(uuid: String): EntityPlayer? =
            MinecraftServer
                .getServer()
                .configurationManager
                .playerEntityList
                .firstOrNull { it.uniqueID.toString() == uuid }
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
    internal fun readFromNBT(nbtTag: NBTTagCompound) =
        apply {
            selectorIndex = nbtTag.getInteger(TAG_EXECUTOR_GENERATOR)
            matcherIndex = nbtTag.getInteger(TAG_BLOCK_FILTER)
            stopVeinMiningOnKeyRelease = nbtTag.getBoolean(TAG_STOP_VEIN_MINING_ON_KEY_RELEASE)
        }

    /**
     * Write to "WorldSavedData".
     */
    internal fun writeToNBT(nbtTag: NBTTagCompound) =
        apply {
            nbtTag.setInteger(TAG_EXECUTOR_GENERATOR, selectorIndex)
            nbtTag.setInteger(TAG_BLOCK_FILTER, matcherIndex)
            nbtTag.setBoolean(TAG_STOP_VEIN_MINING_ON_KEY_RELEASE, stopVeinMiningOnKeyRelease)
        }
}

internal val EntityPlayer.veinMiningData: VeinMiningPlayerData get() = VeinMiningPlayerData.getOrCreate(this)

internal val EntityPlayer.asMP: EntityPlayerMP get() = this as EntityPlayerMP
