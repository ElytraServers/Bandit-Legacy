package cn.elytra.mod.bandit.common.mining

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.WorldSavedData
import net.minecraftforge.common.DimensionManager

class VeinMiningDataSave(name: String) : WorldSavedData(name) {

    companion object {
        const val DATA_NAME = "VeinMiningDataSave"

        const val TAG_PLAYER_DATA = "VeinPlayerData"

        fun get(): VeinMiningDataSave {
            val world = requireNotNull(DimensionManager.getWorld(0)) { "Dimension 0 is null? How?" }

            var data =
                world.loadItemData(VeinMiningDataSave::class.java, DATA_NAME) as VeinMiningDataSave?
            if(data == null) {
                data = VeinMiningDataSave(DATA_NAME)
                world.setItemData(DATA_NAME, data)
            }
            return data
        }
    }

    private var playerData: NBTTagCompound = NBTTagCompound()

    override fun readFromNBT(nbt: NBTTagCompound) {
        playerData = nbt.getCompoundTag(TAG_PLAYER_DATA)
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        nbt.setTag(TAG_PLAYER_DATA, playerData)
    }

    fun getPlayerData(uuid: String): NBTTagCompound {
        return playerData.getCompoundTag(uuid)
    }

    fun setPlayerData(uuid: String, data: NBTTagCompound) {
        playerData.setTag(uuid, data)
    }
}
