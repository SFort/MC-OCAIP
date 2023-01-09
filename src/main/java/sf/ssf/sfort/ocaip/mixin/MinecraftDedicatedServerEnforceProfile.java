package sf.ssf.sfort.ocaip.mixin;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftDedicatedServer.class)
public class MinecraftDedicatedServerEnforceProfile {
	@Inject(at=@At("HEAD"), method="shouldEnforceSecureProfile()Z", cancellable=true)
	public void dontEnforceSecureProfile(CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(false);
	}
}
