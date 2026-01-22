package cn.elytra.mod.bandit.client

import cn.elytra.mod.bandit.BanditMod
import cn.elytra.mod.bandit.MixinBridger
import cn.elytra.mod.bandit.client.VeinMiningHandlerClient.keyPressed
import cn.elytra.mod.bandit.client.VeinMiningHandlerClient.matcherIndex
import cn.elytra.mod.bandit.client.VeinMiningHandlerClient.onMouseInput
import cn.elytra.mod.bandit.client.VeinMiningHandlerClient.selectorIndex
import cn.elytra.mod.bandit.common.registry.Named
import cn.elytra.mod.bandit.network.BanditNetwork
import cn.elytra.mod.bandit.util.remEuclid
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber
import cpw.mods.fml.client.event.ConfigChangedEvent
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.InputEvent
import cpw.mods.fml.common.gameevent.TickEvent
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.client.event.RenderWorldLastEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import kotlin.properties.Delegates

/**
 * The object that handles all the things about Vein Mining on the client side.
 *
 * [selectorIndex] stores the id of selected executor.
 * [matcherIndex] stores the id of selected block filter.
 *
 * They are all synchronized on login, by the server. And if they're modified, they need to be sync-ed manually.
 *
 * [keyPressed] stores the status of the trigger key in the last tick. If the value is changed at this tick, it will be
 * sync-ed to server.
 *
 * [onMouseInput] handles the mouse wheel operations, passing the data to [VeinMiningHUD] for the executor/filter selecting.
 *
 * @see VeinMiningHUD
 */
@Suppress("unused")
@EventBusSubscriber
object VeinMiningHandlerClient {
    var selectorIndex by Delegates.observable(0) { _, oldValue, newValue ->
        check(newValue in selectorNames.indices)
        if (oldValue != newValue) {
            BanditNetwork.syncSettingsToServer(selectorIndex = newValue)
        }
    }
    var matcherIndex by Delegates.observable(0) { _, oldValue, newValue ->
        check(newValue in matcherNames.indices)
        if (oldValue != newValue) {
            BanditNetwork.syncSettingsToServer(matcherIndex = newValue)
        }
    }

    var matcherNames: List<Named<*>> = listOf(Named("DATA ERROR", "?", Unit))
    var selectorNames: List<Named<*>> = listOf(Named("DATA ERROR", "?", Unit))

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
    fun onKeyInput(e: InputEvent.KeyInputEvent) {
        if (Minecraft.getMinecraft().thePlayer == null) return

        val keyPressedNow =
            if (statusKey.keyCode >= 0) {
                Keyboard.isKeyDown(statusKey.keyCode)
            } else {
                val button = Mouse.getEventButton()
                if (button == -1) {
                    keyPressed
                } else {
                    statusKey.keyCode + 100 == button && Mouse.getEventButtonState()
                }
            }
        if (keyPressedNow != keyPressed) {
            BanditNetwork.syncStatusToServer(keyPressedNow)
            keyPressed = keyPressedNow
        }
    }

    fun onMouseInput(d: Int): Boolean {
        val delta =
            when {
                !keyPressed -> return false
                d < 0 -> 1
                d > 0 -> -1
                else -> return false
            }
        if (GuiScreen.isShiftKeyDown()) {
            selectorIndex = (selectorIndex + delta) remEuclid selectorNames.size
        } else if (GuiScreen.isCtrlKeyDown()) {
            matcherIndex = (matcherIndex + delta) remEuclid matcherNames.size
        }
        return true
    }

    @JvmStatic
    @SubscribeEvent
    fun onRenderHUD(e: TickEvent.RenderTickEvent) {
        if (e.phase != TickEvent.Phase.END) return
        if (keyPressed) {
            VeinMiningHUD.render()
        }
    }

    @JvmStatic
    @SubscribeEvent
    fun onRenderSelectedBlock(e: RenderWorldLastEvent) {
        val selectedBlockPosList = selectedBlockPosList
        if (keyPressed && !selectedBlockPosList.isNullOrEmpty()) {
            if (selectedBlockPosList.size <= maxSelectionRenderCount) {
                VeinMiningSelectionRenderer.render(e, selectedBlockPosList)
            }
        }
    }

    @JvmStatic
    @SubscribeEvent
    fun onConfigReloaded(e: ConfigChangedEvent.PostConfigChangedEvent) {
        if (e.modID == BanditMod.MOD_ID) {
            BanditMod.logger.info("Refreshing configuration")
            VeinMiningConfigClient.reload()
            VeinMiningConfigClient.save()
        }
    }
}
