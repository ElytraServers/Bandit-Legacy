package cn.elytra.mod.bandit

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.SidedProxy
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.event.FMLServerStartingEvent
import cpw.mods.fml.common.event.FMLServerStoppingEvent
import cpw.mods.fml.common.eventhandler.EventBus
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Suppress("unused")
@Mod(
    modid = BanditMod.MOD_ID,
    name = BanditMod.NAME,
    version = "TEST_VERSION", //Tags.VERSION,
    modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter",
    guiFactory = "cn.elytra.mod.bandit.client.BanditConfigGuiFactory",
)
object BanditMod {

    const val MOD_ID = "bandit"
    const val NAME = "Bandit Legacy"

    val logger: Logger = LogManager.getLogger()

    @SidedProxy(
        serverSide = "cn.elytra.mod.bandit.CommonProxy",
        clientSide = "cn.elytra.mod.bandit.ClientProxy",
    )
    lateinit var proxy: CommonProxy

    val bus = EventBus()

    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        proxy.preInit(e)
    }

    @Mod.EventHandler
    fun serverStarting(e: FMLServerStartingEvent) {
        proxy.serverStarting(e)
    }

    @Mod.EventHandler
    fun serverStopping(e: FMLServerStoppingEvent) {
        proxy.serverStopping(e)
    }

}
