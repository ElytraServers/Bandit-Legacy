package cn.elytra.mod.bandit.util

import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.world.WorldServer

object MinecraftUtils {
    fun findPlayerByUniqueId(id: String): EntityPlayerMP? =
        MinecraftServer
            .getServer()
            .configurationManager.playerEntityList
            .firstOrNull { it.uniqueID.toString() == id }

    fun findWorldByDimId(dimId: Int): WorldServer? =
        MinecraftServer
            .getServer()
            .worldServers
            .firstOrNull { it.provider.dimensionId == dimId }
}
