package cn.elytra.mod.bandit.client

import cn.elytra.mod.bandit.BanditMod
import cn.elytra.mod.bandit.MixinBridger
import cn.elytra.mod.bandit.client.VeinMiningHandlerClient.keyPressed
import cn.elytra.mod.bandit.client.VeinMiningHandlerClient.onMouseInput
import cn.elytra.mod.bandit.client.VeinMiningHandlerClient.veinMiningBlockFilterId
import cn.elytra.mod.bandit.client.VeinMiningHandlerClient.veinMiningExecutorId
import cn.elytra.mod.bandit.network.BanditNetwork
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber
import cpw.mods.fml.client.event.ConfigChangedEvent
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.client.event.RenderWorldLastEvent
import org.lwjgl.input.Keyboard

/**
 * The object that handles all the things about Vein Mining on the client side.
 *
 * [veinMiningExecutorId] stores the id of selected executor.
 * [veinMiningBlockFilterId] stores the id of selected block filter.
 *
 * They are all synchronized on login, by the server. And if they're modified, they need to be sync-ed manually.
 *
 * [keyPressed] stores the status of the trigger key in the last tick. If the value is changed at this tick, it will be
 * sync-ed to server.
 *
 * [onMouseInput] handles the mouse wheel operations, passing the data to [VeinMiningHUD] for the executor/filter selecting.
 *
 * @see VeinMiningHUD
 * @see cn.elytra.mod.bandit.common.mining.VeinMiningHandler
 */
@Suppress("unused")
@EventBusSubscriber
object VeinMiningHandlerClient {

    var veinMiningExecutorId = 0
    var veinMiningBlockFilterId = 0

    var keyPressed = false

    val statusKey =
        KeyBinding("keybinding.bandit.trigger", Keyboard.KEY_NONE, "keybinding.bandit.category")

    // sync from server
    var selectedBlockPosList: List<BlockPos>? = null

    /**
     * The max count of block positions to render
     */
    var maxSelectionRenderCount = 1024

    init {
        // register the callback
        MixinBridger.onMouseScrollCancelable += this::onMouseInput
    }

    @JvmStatic
    @SubscribeEvent
    fun onClientTick(e: TickEvent.ClientTickEvent) {
        if(e.phase != TickEvent.Phase.START) return
        if(Minecraft.getMinecraft().thePlayer == null) return

        val keyPressedNow = Keyboard.isKeyDown(statusKey.keyCode)
        if(keyPressedNow != keyPressed) {
            BanditNetwork.syncStatusToServer(keyPressedNow)
            keyPressed = keyPressedNow
        }
    }

    fun onMouseInput(d: Int): Boolean {
        VeinMiningHUD.withActiveMenu {
            if(d < 0) {
                this.move(1)
                return true
            } else if (d > 0) {
                this.move(-1)
                return true
            }
        }
        return false
    }

    @JvmStatic
    @SubscribeEvent
    fun onRenderHUD(e: TickEvent.RenderTickEvent) {
        if(e.phase != TickEvent.Phase.END) return
        if(keyPressed) {
            VeinMiningHUD.render()
        }
    }

    @JvmStatic
    @SubscribeEvent
    fun onRenderSelectedBlock(e: RenderWorldLastEvent) {
        val selectedBlockPosList = selectedBlockPosList
        if(keyPressed && selectedBlockPosList != null && selectedBlockPosList.isNotEmpty()) {
            if(selectedBlockPosList.size <= maxSelectionRenderCount) {
                VeinMiningSelectionRenderer.render(e, selectedBlockPosList)
            }
        }
    }

    @JvmStatic
    @SubscribeEvent
    fun onConfigReloaded(e: ConfigChangedEvent.PostConfigChangedEvent) {
        if(e.modID == BanditMod.MOD_ID) {
            BanditMod.logger.info("Refreshing configuration")
            VeinMiningConfigClient.reload()
            VeinMiningConfigClient.save()
        }
    }

}
