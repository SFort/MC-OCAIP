package sf.ssf.sfort.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class Server {
	@Final @Shadow
	public final boolean onlineMode = false;
	@Inject(method = "setOnlineMode(Z)V", at=@At("HEAD"), cancellable = true)
	public void setOnlineMode(boolean onlineMode, CallbackInfo ci){
		ci.cancel();
	}
	@Inject(method = "isOnlineMode()Z", at=@At("HEAD"), cancellable = true)
	public void isOnlineMode(CallbackInfoReturnable<Boolean> cir){
		cir.setReturnValue(false);
	}
}