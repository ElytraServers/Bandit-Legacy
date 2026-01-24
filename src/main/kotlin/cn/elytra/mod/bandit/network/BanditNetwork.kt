package cn.elytra.mod.bandit.network

import cn.elytra.mod.bandit.BanditMod
import cn.elytra.mod.bandit.common.player_data.VeinMiningNoticeType
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
import cpw.mods.fml.relauncher.Side
import net.minecraft.entity.player.EntityPlayerMP

object BanditNetwork {

    private val networkW = SimpleNetworkWrapper(BanditMod.MOD_ID)

    @Suppress("unused")
    fun register(unused: FMLPreInitializationEvent) {
        networkW.registerMessage(
            C2SStatusPacket.Handler::class.java, C2SStatusPacket::class.java, 0, Side.SERVER
        )
        networkW.registerMessage(
            SettingsExchangePacket.ServerHandler::class.java,
            SettingsExchangePacket::class.java,
            1,
            Side.SERVER
        )
        networkW.registerMessage(
            SettingsExchangePacket.ClientHandler::class.java,
            SettingsExchangePacket::class.java,
            2,
            Side.CLIENT
        )

        networkW.registerMessage(
            S2CSelectedBlockCachePacket.Handler::class.java,
            S2CSelectedBlockCachePacket::class.java,
            3,
            Side.CLIENT
        )

        networkW.registerMessage(
            S2CNoticePacket.Handler::class.java,
            S2CNoticePacket::class.java,
            4,
            Side.CLIENT
        )
    }

    fun syncStatusToServer(status: Boolean) {
        networkW.sendToServer(C2SStatusPacket().apply { this.status = status })
    }

    fun syncSettingsToServer(executorId: Int? = null, blockFilterId: Int? = null) {
        if(executorId != null || blockFilterId != null) {
            networkW.sendToServer(
                SettingsExchangePacket().apply {
                    this.executorId = executorId
                    this.blockFilterId = blockFilterId
                })
        }
    }

    fun syncSettingsToClient(p: EntityPlayerMP, executorId: Int? = null, blockFilterId: Int? = null) {
        if(executorId != null || blockFilterId != null) {
            networkW.sendTo(
                SettingsExchangePacket().apply {
                    this.executorId = executorId
                    this.blockFilterId = blockFilterId
                },
                p
            )
        }
    }

    fun syncBlockCacheToClient(p: EntityPlayerMP, posList: List<BlockPos>?) {
        networkW.sendTo(S2CSelectedBlockCachePacket().apply { this.posList = posList ?: emptyList() }, p)
    }

    /**
     * 发送简单通知
     * 用于：任务开始、提示可以停止、按键释放停止
     * 返回 noticeId，用于后续调用 endNoticeToClient 来停止渲染
     */
    fun pushSimpleNoticeToClient(p: EntityPlayerMP, noticeType: VeinMiningNoticeType, fadeDelay: Int = 0, fadeTicks: Int = 0): Long {
        val noticeId = System.nanoTime()
        networkW.sendTo(
            S2CNoticePacket().apply {
                action = S2CNoticePacket.NoticeAction.PUSH
                this.noticeId = noticeId
                this.noticeType = noticeType.id
                this.extraData = emptyMap()
                this.fadeDelay = fadeDelay
                this.fadeTicks = fadeTicks
            },
            p
        )
        return noticeId
    }

    /**
     * 发送任务完成通知
     * 用于：任务完成
     * 此通知会自动渐隐消失，不需要额外调用清除方法
     */
    fun pushCompletionNoticeToClient(
        p: EntityPlayerMP,
        extraData: Map<String, Int>,
        fadeDelay: Int = 40,
        fadeTicks: Int = 20
    ) : Long{
        val noticeId = System.nanoTime()
        networkW.sendTo(
            S2CNoticePacket().apply {
                action = S2CNoticePacket.NoticeAction.PUSH
                this.noticeId = noticeId
                this.noticeType = VeinMiningNoticeType.TASK_DONE.id
                this.extraData = extraData
                this.fadeDelay = fadeDelay
                this.fadeTicks = fadeTicks
            },
            p
        )
        return noticeId
    }

    fun endNoticeToClient(p: EntityPlayerMP, noticeId: Long, fadeDelay: Int = 0, fadeTicks: Int = 0) {
        networkW.sendTo(
            S2CNoticePacket().apply {
                action = S2CNoticePacket.NoticeAction.END
                this.noticeId = noticeId
                this.fadeDelay = fadeDelay
                this.fadeTicks = fadeTicks
            },
            p
        )
    }
}
