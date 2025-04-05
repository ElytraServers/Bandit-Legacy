package cn.elytra.mod.bandit.mixin;

import cn.elytra.mod.bandit.MixinBridger;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;changeCurrentItem(I)V"), cancellable = true)
    private void bandit$hookMouseScroll(CallbackInfo ci, @Local(ordinal = 1) int i) {
        var shouldCancel = MixinBridger.fireMouseScrollCancelable(i);
        if(shouldCancel) {
            ci.cancel();
        }
    }

}
