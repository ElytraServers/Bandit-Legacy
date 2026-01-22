package cn.elytra.mod.bandit

import cpw.mods.fml.common.Loader
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
    version = Tags.VERSION,
    dependencies = BanditMod.DEPENDENCIES,
    modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter",
    guiFactory = "cn.elytra.mod.bandit.client.BanditConfigGuiFactory",
)
object BanditMod {
    const val MOD_ID = "bandit"
    const val NAME = "Bandit Legacy"

    const val DEPENDENCIES =
        "required-after:forgelin@[2.0.0-GTNH,);" + // Kotlin Coroutines 1.9.0+ Required for Flow<T>.chunked()
            "required-after:gtnhlib@[0.5.21,);" + // GTNHLib 0.5.21 used for GTNH 2.7.0
            "required-after:CodeChickenCore;" // CCC used for rendering

    val logger: Logger = LogManager.getLogger()

    @SidedProxy(
        serverSide = "cn.elytra.mod.bandit.CommonProxy",
        clientSide = "cn.elytra.mod.bandit.ClientProxy",
    )
    lateinit var proxy: CommonProxy

    val bus = EventBus()

    init {
        fun logMod(id: String) {
            try {
                if (Loader.isModLoaded(id)) {
                    val mod = Loader.instance().modList.first { it.modId == id }
                    val version = mod.version
                    val metadataVersion = mod.metadata.version
                    logger.debug("Mod {} found with version {} (metadata {})", id, version, metadataVersion)
                }
            } catch (e: Exception) {
                logger.warn("Failed to inspect the information of {}", id, e)
            }
        }

        // hard dependencies
        logMod("forgelin")
        logMod("gtnhlib")
        logMod("CodeChickenCore")
        // soft dependencies
        logMod("gregtech")
    }

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
