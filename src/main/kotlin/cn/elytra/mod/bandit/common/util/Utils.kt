package cn.elytra.mod.bandit.common.util

import gnu.trove.map.hash.TCustomHashMap
import gnu.trove.strategy.HashingStrategy
import net.minecraft.item.ItemStack
import java.util.*

internal inline fun <reified T : Enum<T>> parseValueToEnum(value: String): T? {
    return enumValues<T>().firstOrNull { it.name.lowercase() == value.lowercase() }
}

internal inline fun <reified T : Enum<T>> parseValueToEnum(value: String, defaultValue: T): T {
    return enumValues<T>().firstOrNull { it.name.lowercase() == value.lowercase() } ?: defaultValue
}

object ItemStackHashingStrategy : HashingStrategy<ItemStack> {
    @Suppress("unused")
    private fun readResolve(): Any = ItemStackHashingStrategy

    override fun computeHashCode(itemstack: ItemStack?): Int {
        return when(itemstack) {
            null -> 0
            else -> Objects.hash(itemstack.item, itemstack.itemDamage, itemstack.tagCompound)
        }
    }

    override fun equals(itemstack1: ItemStack?, itemstack2: ItemStack?): Boolean {
        return ItemStack.areItemStacksEqual(itemstack1, itemstack2)
    }
}

internal fun <T> newItemStackHashMap(): MutableMap<ItemStack, T> {
    return TCustomHashMap<ItemStack, T>(ItemStackHashingStrategy)
}
