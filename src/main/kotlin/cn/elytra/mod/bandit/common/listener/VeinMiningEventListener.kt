package cn.elytra.mod.bandit.common.listener

import cn.elytra.mod.bandit.common.player_data.veinMiningData
import cn.elytra.mod.bandit.common.registry.BanditRegistration
import cn.elytra.mod.bandit.mining2.VeinMiningPlayerHandle
import cn.elytra.mod.bandit.mining2.updateContextByPlayerRaytrace
import cn.elytra.mod.bandit.network.BanditNetwork
import cn.elytra.mod.bandit.util.HarvestCollector
import cn.elytra.mod.bandit.util.newBlockSnapshot
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.PlayerEvent
import cpw.mods.fml.common.gameevent.TickEvent
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatComponentTranslation
import net.minecraftforge.common.util.FakePlayer
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.world.BlockEvent

@Suppress("unused")
@EventBusSubscriber
class VeinMiningEventListener {
    companion object {
        private fun BlockEvent.getBlockPos(): BlockPos = BlockPos(x, y, z)

        @JvmStatic
        @SubscribeEvent
        fun onBlockBreaking(e: BlockEvent.BreakEvent) {
            val p = e.player
            if (p is EntityPlayerMP && p !is FakePlayer) {
                val h = VeinMiningPlayerHandle.get(p)
                if (h.canUpdateContext()) {
                    h.updateContext(newBlockSnapshot(p.worldObj, e.getBlockPos()))
                    if (h.activatedVeinMining) {
                        h.startVeinMining()
                        e.isCanceled = true
                    }
                }
            }
        }

        @JvmStatic
        @SubscribeEvent
        fun onPlayerTick(e: TickEvent.PlayerTickEvent) {
            val p = e.player
            if (p is EntityPlayerMP && p !is FakePlayer) {
                val h = VeinMiningPlayerHandle.get(p)
                if (h.canUpdateContext()) {
                    h.updateContextByPlayerRaytrace(p)
                    if (h.activatedVeinMining) {
                        h.startBlockFinding()
                    }
                }
            }
        }

        @JvmStatic
        @SubscribeEvent
        fun onEntitySpawn(e: EntityJoinWorldEvent) {
            if (!HarvestCollector.shouldCollect) return

            val entity = e.entity
            // if the entity is already dead (removed), ignore it
            if (entity.isDead) return
            // mark the drops and exps and remove them
            if (entity is EntityItem) {
                val item: ItemStack? = entity.dataWatcher.getWatchableObjectItemStack(10)
                if (item != null) {
                    HarvestCollector.addItemStack(item)
                    entity.setDead()
                    e.isCanceled = true
                }
            } else if (entity is EntityXPOrb) {
                HarvestCollector.addXpValue(entity.xpValue)
                entity.setDead()
                e.isCanceled = true
            }
        }

        @JvmStatic
        @SubscribeEvent
        fun onPlayerJoin(e: PlayerEvent.PlayerLoggedInEvent) {
            val p = e.player
            if (p is EntityPlayerMP) {
                val matcher = p.veinMiningData.matcherIndex
                val selector = p.veinMiningData.selectorIndex
                BanditNetwork.syncServerRegistrationToClient(p)
                BanditNetwork.syncSettingsToClient(p, selector, matcher)
                p.addChatMessage(
                    // TODO: REMOVE THIS
                    ChatComponentText(
                        "Server registration [DEBUG]: matchers " +
                            BanditRegistration.matchers.joinToString(",", "[", "]") { it.name } +
                            " selectors " +
                            BanditRegistration.selectors.joinToString(",", "[", "]") { it.name },
                    ),
                )
                p.addChatMessage(
                    ChatComponentTranslation(
                        "bandit.message.sync-from-server",
                        ChatComponentTranslation(BanditRegistration.getMatcher(matcher).translationKey),
                        ChatComponentTranslation(BanditRegistration.getSelector(selector).translationKey),
                    ),
                )
            }
        }

        @JvmStatic
        @SubscribeEvent
        fun onPlayerLeave(e: PlayerEvent.PlayerLoggedOutEvent) {
            VeinMiningPlayerHandle.removeByEvent(e)
        }
    }
}
