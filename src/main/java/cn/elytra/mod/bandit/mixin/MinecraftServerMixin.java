package cn.elytra.mod.bandit.mixin;

import cn.elytra.mod.bandit.common.BanditCoroutines;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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

    @WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;sleep(J)V"))
    private void bandit$hookThreadSleep(long timeToSleep, Operation<Void> original) {
        long newTimeToSleep = BanditCoroutines.onMainThreadAboutToSleep$bandit(timeToSleep);
        if(newTimeToSleep > 0) original.call(newTimeToSleep);
    }

}
