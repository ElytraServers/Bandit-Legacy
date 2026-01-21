package cn.elytra.mod.bandit.common.registry

import net.minecraft.nbt.NBTTagCompound

/**
 * A data wrapper that gives the object a name and a translation key for localization.
 */
data class Named<T>(
    val name: String,
    val translationKey: String,
    val value: T,
) {
    fun asTag(): NBTTagCompound =
        NBTTagCompound().apply {
            setString("name", name)
            setString("translationKey", translationKey)
        }

    companion object {
        internal fun fromTag(tag: NBTTagCompound): Named<*> =
            Named(
                tag.getString("name"),
                tag.getString("translationKey"),
                Unit,
            )
    }
}
