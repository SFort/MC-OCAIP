package sf.ssf.sfort.ocaip.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestPayload;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sf.ssf.sfort.ocaip.OldCustomPayload;

//Priority to overrule fapi
@Mixin(value=LoginQueryRequestS2CPacket.class, priority=200)
public class OldLoginPacketClientMixin {

	@Inject(method="readPayload(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/network/packet/s2c/login/LoginQueryRequestPayload;", at=@At("HEAD"), cancellable=true)
	private static void oldPayload(Identifier id, PacketByteBuf buf, CallbackInfoReturnable<LoginQueryRequestPayload> cir){
		if ("ocaip".equals(id.getNamespace())) {
			cir.setReturnValue(new OldCustomPayload.LoginRequest(buf));
		}
	}
}
