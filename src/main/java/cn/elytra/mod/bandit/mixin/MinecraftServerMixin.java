package cn.elytra.mod.bandit.mixin;

import cn.elytra.mod.bandit.common.BanditCoroutines;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "updateTimeLightAndEntities", at = @At("HEAD"))
    private void bandit$hookTick(CallbackInfo ci) {
        BanditCoroutines.INSTANCE.getTicking().run();
    }

}
