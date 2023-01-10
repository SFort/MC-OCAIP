package sf.ssf.sfort.ocaip.mixin;

import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sf.ssf.sfort.ocaip.Tape;

import java.util.UUID;

@Mixin(DefaultSkinHelper.class)
public class SkinHelper {
	@Inject(method="getTexture(Ljava/util/UUID;)Lnet/minecraft/util/Identifier;", at=@At("HEAD"), cancellable=true)
	private static void ocaip$uuidIdentifier(UUID uuid, CallbackInfoReturnable<Identifier> cir) {
		String uu = uuid.toString();
		if (Tape.hasOfflineSkin(uu)) {
			cir.setReturnValue(new Identifier("ocaip_skin", uu));
		}
	}
	@Inject(method="getModel(Ljava/util/UUID;)Ljava/lang/String;", at=@At("HEAD"), cancellable=true)
	private static void ocaip$offlineModel(UUID uuid, CallbackInfoReturnable<String> cir) {
		if (Tape.hasOfflineSkin(uuid.toString())) {
			cir.setReturnValue("default");
		}
	}
}
