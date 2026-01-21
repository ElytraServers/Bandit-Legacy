package cn.elytra.mod.bandit.common.listener

import cn.elytra.mod.bandit.common.player_data.veinMiningData
import cn.elytra.mod.bandit.common.registry.BanditRegisteration
import cn.elytra.mod.bandit.mining2.MiningV2
import cn.elytra.mod.bandit.mining2.session.isRunning
import cn.elytra.mod.bandit.network.BanditNetwork
import cn.elytra.mod.bandit.util.HarvestCollector
import codechicken.lib.raytracer.RayTracer
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
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.common.util.FakePlayer
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
            if (p is EntityPlayerMP && p !is FakePlayer) {
                val session = MiningV2.getSession(p)
                if (session.isRunning()) return

                if (p.veinMiningData.veinMiningKeyPressed) {
                    MiningV2.newSession(p, BlockPos(e.x, e.y, e.z)).startVeinMining()
                    e.isCanceled = true
                }
            }
        }

        @JvmStatic
        @SubscribeEvent
        fun onPlayerTick(e: TickEvent.PlayerTickEvent) {
            val p = e.player
            if (p is EntityPlayerMP && p !is FakePlayer) {
                val session = MiningV2.getSession(p)
                if (session.isRunning()) return

                if (p.veinMiningData.veinMiningKeyPressed) {
                    val mop = RayTracer.reTrace(p.worldObj, p)
                    if (mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return

                    MiningV2.newSession(p, BlockPos(mop.blockX, mop.blockY, mop.blockZ)).startFindPositions()
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
                p.addChatMessage( // TODO: REMOVE THIS
                    ChatComponentText(
                        "Server registration [DEBUG]: matchers " +
                            BanditRegisteration.matchers.joinToString(",", "[", "]") { it.name } +
                            " selectors " +
                            BanditRegisteration.selectors.joinToString(",", "[", "]") { it.name },
                    ),
                )
                p.addChatMessage(
                    ChatComponentTranslation(
                        "bandit.message.sync-from-server",
                        ChatComponentTranslation(BanditRegisteration.getMatcher(matcher).translationKey),
                        ChatComponentTranslation(BanditRegisteration.getSelector(selector).translationKey),
                    ),
                )
            }
        }

        @JvmStatic
        @SubscribeEvent
        fun onPlayerLeave(e: PlayerEvent.PlayerLoggedOutEvent) {
            MiningV2.dropAny(e.player)
        }
    }
}
