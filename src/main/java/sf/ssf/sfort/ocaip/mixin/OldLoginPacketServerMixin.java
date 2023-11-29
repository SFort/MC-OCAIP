package sf.ssf.sfort.ocaip.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponsePayload;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sf.ssf.sfort.ocaip.OldCustomPayload;

@Mixin(LoginQueryResponseC2SPacket.class)
public class OldLoginPacketServerMixin {

	@Inject(method="readPayload(ILnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/network/packet/c2s/login/LoginQueryResponsePayload;", at=@At("HEAD"), cancellable=true)
	private static void oldPayload(int queryId, PacketByteBuf buf, CallbackInfoReturnable<LoginQueryResponsePayload> cir){
		if (queryId == 41809950 || queryId == 41809951 || queryId == 41809952) {
			if (buf.readBoolean()) {
				cir.setReturnValue(new OldCustomPayload.LoginResponse(buf));
			} else {
				cir.setReturnValue(new OldCustomPayload.LoginResponse(new Identifier("ocaip_null", "vanilla_client"), null));
			}
		}
	}
}
