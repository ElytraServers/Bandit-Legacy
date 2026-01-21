package cn.elytra.mod.bandit.network

import cn.elytra.mod.bandit.BanditMod
import cn.elytra.mod.bandit.client.VeinMiningHandlerClient
import cn.elytra.mod.bandit.common.registry.Named
import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf

class S2CRegistrationSyncPacket : IMessage {
    var matcherNames: List<Named<*>> = emptyList()
    var selectorNames: List<Named<*>> = emptyList()

    override fun fromBytes(buf: ByteBuf) {
        matcherNames =
            List(buf.readInt()) {
                Named.fromTag(ByteBufUtils.readTag(buf))
            }
        selectorNames =
            List(buf.readInt()) {
                Named.fromTag(ByteBufUtils.readTag(buf))
            }
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(matcherNames.size)
        matcherNames.forEach {
            ByteBufUtils.writeTag(buf, it.asTag())
        }
        buf.writeInt(selectorNames.size)
        selectorNames.forEach {
            ByteBufUtils.writeTag(buf, it.asTag())
        }
    }

    class Handler : IMessageHandler<S2CRegistrationSyncPacket, IMessage> {
        override fun onMessage(
            message: S2CRegistrationSyncPacket,
            ctx: MessageContext,
        ): IMessage? {
            VeinMiningHandlerClient.matcherNames = message.matcherNames
            VeinMiningHandlerClient.selectorNames = message.selectorNames
            BanditMod.logger.info(
                "Received server registration: matchers {} selectors {}",
                message.matcherNames,
                message.selectorNames,
            )
            return null
        }
    }
}
