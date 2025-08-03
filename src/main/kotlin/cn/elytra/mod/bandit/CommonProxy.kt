package cn.elytra.mod.bandit

import cn.elytra.mod.bandit.common.BanditCoroutines
import cn.elytra.mod.bandit.common.command.BanditCommand
import cn.elytra.mod.bandit.common.listener.VeinMiningEventListener
import cn.elytra.mod.bandit.compat.GT5UCompat
import cn.elytra.mod.bandit.network.BanditNetwork
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.event.FMLServerStartingEvent
import cpw.mods.fml.common.event.FMLServerStoppingEvent
import kotlinx.coroutines.cancel
import net.minecraftforge.common.MinecraftForge

open class CommonProxy {

    open fun preInit(e: FMLPreInitializationEvent) {
        BanditMod.logger.info("Initializing network")
        BanditNetwork.register(e)
        BanditConfig.init(e)

        subscribeCommonBuses(VeinMiningEventListener)

        //? if 1.7.10 {
        if(Loader.isModLoaded("gregtech")) {
            GT5UCompat.init()
            BanditMod.logger.info("GT5UCompat loaded")
        }
        //? }
    }

    open fun serverStarting(e: FMLServerStartingEvent) {
        BanditMod.logger.info("Initializing commands")
        e.registerServerCommand(BanditCommand)

        BanditMod.logger.info("Initializing coroutines")
        BanditCoroutines.initServer(e.server)
    }

    open fun serverStopping(event: FMLServerStoppingEvent) {
        BanditCoroutines.VeinMiningScope.cancel("server stopping")
    }

    protected fun subscribeCommonBuses(listener: Any) {
        MinecraftForge.EVENT_BUS.register(listener)
        FMLCommonHandler.instance().bus().register(listener)
    }
}
