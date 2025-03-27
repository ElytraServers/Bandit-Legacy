package cn.elytra.mod.bandit.mixin;

import cn.elytra.mod.bandit.common.BanditCoroutines;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void hook$tick(CallbackInfo ci) {
        BanditCoroutines.INSTANCE.getTicking().update();
    }

}
