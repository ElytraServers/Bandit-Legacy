package cn.elytra.mod.bandit

import cn.elytra.mod.bandit.client.VeinMiningHandlerClient
import cpw.mods.fml.client.registry.ClientRegistry
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.event.FMLServerStoppingEvent

class ClientProxy : CommonProxy() {

    override fun preInit(e: FMLPreInitializationEvent) {
        super.preInit(e)

        BanditMod.logger.info("Initializing keybindings")
        ClientRegistry.registerKeyBinding(VeinMiningHandlerClient.statusKey)
    }

    override fun serverStopping(event: FMLServerStoppingEvent) {
        super.serverStopping(event)

        // reset the key
        VeinMiningHandlerClient.keyPressed = false
    }
}
