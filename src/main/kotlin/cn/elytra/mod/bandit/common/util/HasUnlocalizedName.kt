package cn.elytra.mod.bandit.common.util

import net.minecraft.util.ChatComponentTranslation

interface HasUnlocalizedName {

    fun getUnlocalizedName(): String

    fun toChatComponent(): ChatComponentTranslation {
        return ChatComponentTranslation(getUnlocalizedName())
    }

}
