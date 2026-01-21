package cn.elytra.mod.bandit.network

import cn.elytra.mod.bandit.common.player_data.veinMiningData
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf

class C2SStatusPacket : IMessage {
    var status: Boolean = false

    override fun fromBytes(buf: ByteBuf) {
        status = buf.readBoolean()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeBoolean(status)
    }

    class Handler : IMessageHandler<C2SStatusPacket, IMessage> {
        override fun onMessage(
            message: C2SStatusPacket,
            ctx: MessageContext,
        ): IMessage? {
            val p = ctx.serverHandler.playerEntity
            p.veinMiningData.veinMiningKeyPressed = message.status
            return null
        }
    }
}
