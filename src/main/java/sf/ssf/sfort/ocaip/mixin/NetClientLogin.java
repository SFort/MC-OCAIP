package sf.ssf.sfort.ocaip.mixin;

import io.netty.buffer.Unpooled;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sf.ssf.sfort.ocaip.Reel;
import sf.ssf.sfort.ocaip.Tape;

@Mixin(ClientLoginNetworkHandler.class)
public abstract class NetClientLogin {

	boolean ocaip$recivedRequest = false;
	@Shadow @Final private ClientConnection connection;

	@Inject(at=@At("HEAD"), method="joinServerSession(Ljava/lang/String;)Lnet/minecraft/text/Text;", cancellable=true)
	public void ditchYggdrasil(CallbackInfoReturnable<Text> cir) {
		if (ocaip$recivedRequest) {
			cir.setReturnValue(null);
		}
	}

	@Inject(at=@At("HEAD"), method="onQueryRequest(Lnet/minecraft/network/packet/s2c/login/LoginQueryRequestS2CPacket;)V", cancellable=true)
	public void bypassAuthPacket(LoginQueryRequestS2CPacket packet, CallbackInfo ci) {
		if (packet.getQueryId() == 41809951) {
			ocaip$recivedRequest = true;
			PacketByteBuf buf = packet.getPayload();
			if (buf == null) return;
			int version = buf.readVarInt();
			byte[] bytes = buf.readByteArray();
			EdDSAEngine engine = new EdDSAEngine();
			try {
				engine.initSign(Tape.key.getPrivate());
				bytes = engine.signOneShot(bytes);
			} catch (Exception ignore){
				return;
			}
			this.connection.send(new LoginQueryResponseC2SPacket(packet.getQueryId(), new PacketByteBuf(Unpooled.buffer()).writeVarInt(Reel.protocalVersion).writeByteArray(Tape.key.getPublic().getEncoded()).writeByteArray(bytes)));
			ci.cancel();

		}
	}

}