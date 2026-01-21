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

    var matcherIndex: Int? = null
    var selectorIndex: Int? = null

    override fun fromBytes(buf: ByteBuf) {
        matcherIndex = buf.readInt().takeUnless { it == INVALID_VALUE }
        selectorIndex = buf.readInt().takeUnless { it == INVALID_VALUE }
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(matcherIndex ?: INVALID_VALUE)
        buf.writeInt(selectorIndex ?: INVALID_VALUE)
    }

    class ServerHandler : IMessageHandler<SettingsExchangePacket, IMessage> {
        override fun onMessage(
            message: SettingsExchangePacket,
            ctx: MessageContext,
        ): IMessage? {
            val p = ctx.serverHandler.playerEntity
            message.matcherIndex?.let { p.veinMiningData.matcherIndex = it }
            message.selectorIndex?.let { p.veinMiningData.selectorIndex = it }
            return null
        }
    }

    class ClientHandler : IMessageHandler<SettingsExchangePacket, IMessage> {
        override fun onMessage(
            message: SettingsExchangePacket,
            ctx: MessageContext,
        ): IMessage? {
            message.matcherIndex?.let { VeinMiningHandlerClient.matcherIndex = it }
            message.selectorIndex?.let { VeinMiningHandlerClient.selectorIndex = it }
            return null
        }
    }
}
