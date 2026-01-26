package cn.elytra.mod.bandit.network

import cn.elytra.mod.bandit.client.VeinMiningHUD
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf

class S2CNoticePacket : IMessage {

    enum class NoticeAction {
        PUSH,
        END
    }

    var action: NoticeAction = NoticeAction.PUSH
    var noticeId: Long = 0
    var noticeType: Int = 0
    var extraData: Map<String, Int> = emptyMap()
    var fadeDelay: Int = 0
    var fadeTicks: Int = 20

    override fun fromBytes(buf: ByteBuf) {
        action = NoticeAction.entries.toTypedArray()[buf.readByte().toInt()]
        noticeId = buf.readLong()
        noticeType = buf.readInt()

        val extraDataSize = buf.readInt()
        extraData = if (extraDataSize > 0) {
            buildMap {
                repeat(extraDataSize) {
                    val keyLength = buf.readInt()
                    val keyBytes = ByteArray(keyLength)
                    buf.readBytes(keyBytes)
                    val key = String(keyBytes, Charsets.UTF_8)
                    val value = buf.readInt()
                    put(key, value)
                }
            }
        } else {
            emptyMap()
        }

        fadeDelay = buf.readInt()
        fadeTicks = buf.readInt()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeByte(action.ordinal)
        buf.writeLong(noticeId)
        buf.writeInt(noticeType)

        buf.writeInt(extraData.size)
        if (extraData.isNotEmpty()) {
            extraData.forEach { (key, value) ->
                val keyBytes = key.toByteArray(Charsets.UTF_8)
                buf.writeInt(keyBytes.size)
                buf.writeBytes(keyBytes)
                buf.writeInt(value)
            }
        }

        buf.writeInt(fadeDelay)
        buf.writeInt(fadeTicks)
    }

    class Handler : IMessageHandler<S2CNoticePacket, IMessage> {
        override fun onMessage(
            message: S2CNoticePacket,
            ctx: MessageContext,
        ): IMessage? {
            when (message.action) {
                NoticeAction.PUSH -> {
                    VeinMiningHUD.postNotice(
                        message.noticeType,
                        message.noticeId,
                        message.extraData,
                        message.fadeDelay,
                        message.fadeTicks
                    )
                }
                NoticeAction.END -> {
                    VeinMiningHUD.cancelNotice(
                        message.noticeId,
                        message.fadeDelay,
                        message.fadeTicks
                    )
                }
            }
            return null
        }
    }
}

