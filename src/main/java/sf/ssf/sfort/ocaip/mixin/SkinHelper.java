package sf.ssf.sfort.ocaip.mixin;

import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sf.ssf.sfort.ocaip.Tape;

import java.util.UUID;

@Mixin(DefaultSkinHelper.class)
public class SkinHelper {
	@Inject(method="getSkinTextures(Ljava/util/UUID;)Lnet/minecraft/client/util/SkinTextures;", at=@At("HEAD"), cancellable=true)
	private static void ocaip$uuidIdentifier(UUID uuid, CallbackInfoReturnable<SkinTextures> cir) {
		String uu = uuid.toString();
		if (Tape.hasOfflineSkin(uu)) {
			cir.setReturnValue(new SkinTextures(new Identifier("ocaip_skin", uu), null, null, null, SkinTextures.Model.WIDE, true));
		}
	}
}
