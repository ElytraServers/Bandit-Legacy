package cn.elytra.mod.bandit

import cn.elytra.mod.bandit.common.BanditCoroutines
import cn.elytra.mod.bandit.common.command.BanditCommands
import cn.elytra.mod.bandit.compat.GT5UCompat
import cn.elytra.mod.bandit.network.BanditNetwork
import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.event.FMLServerStartingEvent
import cpw.mods.fml.common.event.FMLServerStoppingEvent
import kotlinx.coroutines.cancel

open class CommonProxy {
    open fun preInit(e: FMLPreInitializationEvent) {
        BanditMod.logger.info("Initializing network")
        BanditNetwork.register(e)
        BanditConfig.init(e)

        if (Loader.isModLoaded("gregtech")) {
            GT5UCompat.init()
            BanditMod.logger.info("GT5UCompat loaded")
        }
    }

    open fun serverStarting(e: FMLServerStartingEvent) {
        BanditMod.logger.info("Initializing commands")
        BanditCommands.registerBanditCommand()

        BanditMod.logger.info("Initializing coroutines")
        BanditCoroutines.initServer(e.server)
    }

    open fun serverStopping(event: FMLServerStoppingEvent) {
        BanditCoroutines.VeinMiningScope.cancel("server stopping")
    }
}
