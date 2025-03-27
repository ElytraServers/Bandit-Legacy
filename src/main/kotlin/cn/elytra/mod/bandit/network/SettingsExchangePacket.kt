package cn.elytra.mod.bandit.network

import cn.elytra.mod.bandit.client.VeinMiningHandlerClient
import cn.elytra.mod.bandit.common.player_data.veinMiningData
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf

class SettingsExchangePacket : IMessage {

    companion object {
        const val INVALID_VALUE = -1
    }

    var executorId: Int? = null
    var blockFilterId: Int? = null

    override fun fromBytes(buf: ByteBuf) {
        executorId = buf.readInt().takeUnless { it == INVALID_VALUE }
        blockFilterId = buf.readInt().takeUnless { it == INVALID_VALUE }
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(executorId ?: INVALID_VALUE)
        buf.writeInt(blockFilterId ?: INVALID_VALUE)
    }

    class ServerHandler : IMessageHandler<SettingsExchangePacket, IMessage> {
        override fun onMessage(
            message: SettingsExchangePacket,
            ctx: MessageContext,
        ): IMessage? {
            val p = ctx.serverHandler.playerEntity
            message.executorId?.let { p.veinMiningData.veinMiningExecutorId = it }
            message.blockFilterId?.let { p.veinMiningData.veinMiningBlockFilterId = it }
            return null
        }
    }

    class ClientHandler : IMessageHandler<SettingsExchangePacket, IMessage> {
        override fun onMessage(
            message: SettingsExchangePacket,
            ctx: MessageContext,
        ): IMessage? {
            message.executorId?.let { VeinMiningHandlerClient.veinMiningExecutorId = it }
            message.blockFilterId?.let { VeinMiningHandlerClient.veinMiningBlockFilterId = it }
            return null
        }
    }
}
