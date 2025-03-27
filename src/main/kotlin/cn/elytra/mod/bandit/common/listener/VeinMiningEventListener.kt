package cn.elytra.mod.bandit.common.listener

import cn.elytra.mod.bandit.common.player_data.veinMiningData
import cn.elytra.mod.bandit.mining.HarvestCollector
import cn.elytra.mod.bandit.network.BanditNetwork
import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.PlayerEvent
import cpw.mods.fml.common.gameevent.TickEvent
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.util.ChatComponentTranslation
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.world.BlockEvent

@Suppress("unused")
@EventBusSubscriber
class VeinMiningEventListener {

    companion object {

        @JvmStatic
        @SubscribeEvent
        fun onBlockBreaking(e: BlockEvent.BreakEvent) {
            val p = e.player
            if(p is EntityPlayerMP) {
                val vmData = p.veinMiningData
                if(vmData.hasJobRunning) return
                if(vmData.veinMiningKeyPressed) {
                    // yes, the start of the evil!
                    p.veinMiningData.startVeinMining(e.x, e.y, e.z)
                    e.isCanceled = true
                }
            }
        }

        @JvmStatic
        @SubscribeEvent
        fun onEntitySpawn(e: EntityJoinWorldEvent) {
            if(!HarvestCollector.shouldCollect) return

            val entity = e.entity
            // if the entity is already dead (removed), ignore it
            if(entity.isDead) return
            // mark the drops and exps and remove them
            if(entity is EntityItem) {
                val item: ItemStack? = entity.dataWatcher.getWatchableObjectItemStack(10)
                if(item != null) {
                    HarvestCollector.addItemStack(item)
                    entity.setDead()
                    e.isCanceled = true
                }
            } else if(entity is EntityXPOrb) {
                HarvestCollector.addXpValue(entity.xpValue)
                entity.setDead()
                e.isCanceled = true
            }
        }

        @JvmStatic
        @SubscribeEvent
        fun onPlayerTick(e: TickEvent.PlayerTickEvent) {
            val p = e.player
            if(p is EntityPlayerMP) {
                val vmData = p.veinMiningData
                if(vmData.hasJobRunning) return
                if(vmData.veinMiningKeyPressed) {
                    p.veinMiningData.precalculateVeinBlocks()
                } else {
                    p.veinMiningData.precalculatedVeinBlocks = null
                }
            }
        }

        @JvmStatic
        @SubscribeEvent
        fun onPlayerJoin(e: PlayerEvent.PlayerLoggedInEvent) {
            val p = e.player
            if(p is EntityPlayerMP) {
                val executorId = p.veinMiningData.veinMiningExecutorId
                val filterId = p.veinMiningData.veinMiningBlockFilterId
                BanditNetwork.syncSettingsToClient(p, executorId, filterId)

                // tell the player that the configuration has been changed
                val executor = p.veinMiningData.getExecutorGenerator()
                val filter = p.veinMiningData.getBlockFilter()
                p.addChatMessage(
                    ChatComponentTranslation(
                        "bandit.message.sync-from-server",
                        ChatComponentTranslation(executor.getUnlocalizedName()),
                        ChatComponentTranslation(filter.getUnlocalizedName())
                    )
                )
            }
        }

        @JvmStatic
        @SubscribeEvent
        fun onPlayerLeave(e: PlayerEvent.PlayerLoggedOutEvent) {
            e.player.veinMiningData.stopAndClear()
        }
    }
}
