package cn.elytra.mod.bandit.network

import cn.elytra.mod.bandit.BanditMod
import cn.elytra.mod.bandit.common.registry.BanditRegisteration
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
            C2SStatusPacket.Handler::class.java,
            C2SStatusPacket::class.java,
            0,
            Side.SERVER,
        )
        networkW.registerMessage(
            SettingsExchangePacket.ServerHandler::class.java,
            SettingsExchangePacket::class.java,
            1,
            Side.SERVER,
        )
        networkW.registerMessage(
            SettingsExchangePacket.ClientHandler::class.java,
            SettingsExchangePacket::class.java,
            2,
            Side.CLIENT,
        )

        networkW.registerMessage(
            S2CSelectedBlockCachePacket.Handler::class.java,
            S2CSelectedBlockCachePacket::class.java,
            3,
            Side.CLIENT,
        )

        networkW.registerMessage(
            S2CRegistrationSyncPacket.Handler::class.java,
            S2CRegistrationSyncPacket::class.java,
            4,
            Side.CLIENT,
        )
    }

    fun syncStatusToServer(status: Boolean) {
        networkW.sendToServer(C2SStatusPacket().apply { this.status = status })
    }

    fun syncSettingsToServer(
        selectorIndex: Int? = null,
        matcherIndex: Int? = null,
    ) {
        if (selectorIndex != null || matcherIndex != null) {
            networkW.sendToServer(
                SettingsExchangePacket().apply {
                    this.matcherIndex = matcherIndex
                    this.selectorIndex = selectorIndex
                },
            )
        }
    }

    fun syncSettingsToClient(
        p: EntityPlayerMP,
        selectorIndex: Int? = null,
        matcherIndex: Int? = null,
    ) {
        if (selectorIndex != null || matcherIndex != null) {
            networkW.sendTo(
                SettingsExchangePacket().apply {
                    this.matcherIndex = matcherIndex
                    this.selectorIndex = selectorIndex
                },
                p,
            )
        }
    }

    fun syncBlockCacheToClient(
        p: EntityPlayerMP,
        posList: List<BlockPos>?,
    ) {
        networkW.sendTo(S2CSelectedBlockCachePacket().apply { this.posList = posList ?: emptyList() }, p)
    }

    fun syncServerRegistrationToClient(p: EntityPlayerMP) {
        networkW.sendTo(
            S2CRegistrationSyncPacket().apply {
                this.matcherNames = BanditRegisteration.matchers
                this.selectorNames = BanditRegisteration.selectors
            },
            p,
        )
    }
}
