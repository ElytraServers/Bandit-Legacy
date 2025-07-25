package cn.elytra.mod.bandit

import cpw.mods.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.common.config.Configuration

object BanditConfig {

    lateinit var config: Configuration

    fun init(e: FMLPreInitializationEvent) {
        config = Configuration(e.suggestedConfigurationFile)

        // run all the configuration getters, so that the configuration is properly set up
        try {
            this::class.java.declaredMethods
                .filter { it.isAnnotationPresent(ConfigGetter::class.java) }
                .filter { it.parameterCount == 0 }
                .forEach {
                    runCatching {
                        it.invoke(this)
                        BanditMod.logger.debug("Invoked ${it.name}")
                    }.onFailure { e ->
                        BanditMod.logger.error("Failed to invoke ${it.name}", e)
                    }
                }
        } catch(e: Exception) {
            BanditMod.logger.error("Failed to initialize configuration", e)
        }

        // and save it when we've done
        config.save()
    }

    @Retention(AnnotationRetention.RUNTIME)
    private annotation class ConfigGetter

    val manhattanRadius: Int
        @ConfigGetter get() = config.getInt("manhattan-radius", "executor", 8, 1, Int.MAX_VALUE, "")

    val manhattanLargeRadius: Int
        @ConfigGetter get() = config.getInt("manhattan-large-radius", "executor", 16, 1, Int.MAX_VALUE, "")

    val largeScanRadiusXZ: Int
        @ConfigGetter get() = config.getInt("large-scan-radius-xz", "executor", 32, 1, Int.MAX_VALUE, "")

    val largeScanRadiusY: Int
        @ConfigGetter get() = config.getInt("large-scan-radius-y", "executor", 32, 1, Int.MAX_VALUE, "")

}
