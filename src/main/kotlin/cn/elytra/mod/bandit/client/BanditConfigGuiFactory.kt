package cn.elytra.mod.bandit.client

import cn.elytra.mod.bandit.BanditMod
import cpw.mods.fml.client.IModGuiFactory
import cpw.mods.fml.client.config.GuiConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen

class BanditConfigGuiFactory : IModGuiFactory {

    override fun initialize(minecraftInstance: Minecraft?) {
    }

    override fun mainConfigGuiClass(): Class<out GuiScreen?>? {
        return Gui::class.java
    }

    override fun runtimeGuiCategories(): Set<IModGuiFactory.RuntimeOptionCategoryElement?>? {
        return null
    }

    override fun getHandlerFor(element: IModGuiFactory.RuntimeOptionCategoryElement?): IModGuiFactory.RuntimeOptionGuiHandler? {
        return null
    }

    internal class Gui(parent: GuiScreen) :
        GuiConfig(parent, VeinMiningConfigClient.listElements(), BanditMod.MOD_ID, false, false, "Bandit")
}
