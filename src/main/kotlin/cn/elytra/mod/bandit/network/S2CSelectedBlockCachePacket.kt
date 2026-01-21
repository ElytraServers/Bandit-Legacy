package cn.elytra.mod.bandit.network

import cn.elytra.mod.bandit.client.VeinMiningHandlerClient
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf

class S2CSelectedBlockCachePacket : IMessage {
    var posList: List<BlockPos> = emptyList()

    override fun fromBytes(buf: ByteBuf) {
        val length = buf.readInt()
        this.posList = List(length) { BlockPos().set(buf.readLong()) }
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(posList.size)
        posList.forEach { pos ->
            buf.writeLong(pos.asLong())
        }
    }

    class Handler : IMessageHandler<S2CSelectedBlockCachePacket, IMessage> {
        override fun onMessage(
            message: S2CSelectedBlockCachePacket,
            ctx: MessageContext,
        ): IMessage? {
            VeinMiningHandlerClient.selectedBlockPosList =
                message.posList.takeIf { it.isNotEmpty() }
            return null
        }
    }
}
