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
     * Sends a simple notification to the client.
     *
     * Used for: task starting, stop hint, and key-release stop notifications.
     * If fadeTicks or fadeDelay have not 0 value, this notification automatically fades out
     *  and does not require manual termination.
     * Otherwise, notifications require manual termination via [endNoticeToClient].
     *
     * @param p The target player to receive the notification.
     * @param noticeType The type of notification to display.
     * @param fadeDelay Ticks to wait before starting fade-out animation (0 for immediate fade).
     * @param fadeTicks Duration of fade-out animation in ticks.
     * @return Unique notice ID that can be used to terminate this notification later.
     *
     * @see endNoticeToClient for terminating notifications.
     * @see VeinMiningNoticeType for available notification types.
     */
    fun pushSimpleNoticeToClient(
        p: EntityPlayerMP,
        noticeType: VeinMiningNoticeType,
        fadeDelay: Int = 0,
        fadeTicks: Int = 0
    ): Long {
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
     * Sends a task completion notification to the client.
     *
     * Used for: task completion with statistical data.
     * If fadeTicks or fadeDelay have not 0 value, this notification automatically fades out
     *  and does not require manual termination.
     * Otherwise, notifications require manual termination via [endNoticeToClient].
     *
     * @param p The target player to receive the notification.
     * @param extraData Statistical data to display with the completion notification.
     *                  Contains metrics like blocks mined, experience gained, etc.
     * @param fadeDelay Ticks to wait before starting fade-out animation.
     * @param fadeTicks Duration of fade-out animation in ticks.
     * @return Unique notice ID (primarily for logging/tracking purposes).
     */
    fun pushCompletionNoticeToClient(
        p: EntityPlayerMP,
        extraData: Map<String, Int>,
        fadeDelay: Int = 40,
        fadeTicks: Int = 20
    ): Long {
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

    /**
     * Terminates a previously sent notification on the client.
     *
     * This method stops the rendering of an ongoing notification with a fade-out animation.
     * Should be called for notifications created with [pushSimpleNoticeToClient].
     *
     * @param p The target player whose notification should be terminated.
     * @param noticeId The unique ID of the notification to terminate.
     * @param fadeDelay Ticks to wait before starting the termination fade-out.
     * @param fadeTicks Duration of termination fade-out animation in ticks.
     *
     * @throws NoSuchElementException if no notification with the given ID exists (handled client-side).
     */
    fun endNoticeToClient(
        p: EntityPlayerMP,
        noticeId: Long,
        fadeDelay: Int = 0,
        fadeTicks: Int = 0
    ) {
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
