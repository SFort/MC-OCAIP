package sf.ssf.sfort.ocaip.mixin;

import io.netty.buffer.Unpooled;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sf.ssf.sfort.ocaip.AuthScreen;
import sf.ssf.sfort.ocaip.Reel;
import sf.ssf.sfort.ocaip.Tape;

import java.net.InetSocketAddress;

@Mixin(ClientLoginNetworkHandler.class)
public abstract class NetClientLogin {

	boolean ocaip$recivedRequest = false;
	@Shadow @Final private ClientConnection connection;
	@Shadow @Final private MinecraftClient client;
	@Shadow @Final private @Nullable Screen parentScreen;

	@Inject(at=@At("RETURN"), method="joinServerSession(Ljava/lang/String;)Lnet/minecraft/text/Text;", cancellable=true)
	public void ignoreYggdrasilErr(CallbackInfoReturnable<Text> cir) {
		if (ocaip$recivedRequest) {
			if (cir.getReturnValue() == null){
				this.connection.send(new LoginQueryResponseC2SPacket(41809950, new PacketByteBuf(Unpooled.buffer()).writeVarInt(Reel.protocalVersion)));
			} else {
				cir.setReturnValue(null);
			}
		}
	}

	@Inject(at=@At("HEAD"), method="onQueryRequest(Lnet/minecraft/network/packet/s2c/login/LoginQueryRequestS2CPacket;)V", cancellable=true)
	public void bypassAuthPacket(LoginQueryRequestS2CPacket packet, CallbackInfo ci) {
		int pid = packet.getQueryId();
		if (pid == 41809951 || pid == 41809952) {
			ci.cancel();
			ocaip$recivedRequest = true;
			PacketByteBuf buf = packet.getPayload();
			if (buf == null) return;
			int version = buf.readVarInt();
			if (pid != 41809951 && version <= 1) {
				this.client.setScreen(new DisconnectedScreen(this.parentScreen, Text.of("OCAIP Disconnect"), Text.of("Server running an incompatible version of OCAIP")));
				return;
			}
			byte[] bytes = buf.readByteArray();
			EdDSAEngine engine = new EdDSAEngine();
			try {
				engine.initSign(Tape.key.getPrivate());
				bytes = engine.signOneShot(bytes);
			} catch (Exception ignore){
				return;
			}
			PacketByteBuf tbuf = new PacketByteBuf(Unpooled.buffer()).writeVarInt(Reel.protocalVersion).writeByteArray(Tape.key.getPublic().getEncoded()).writeByteArray(bytes);
			if (pid == 41809952) {
				// 0 - password
				// 1 - sha1 pow
				// 2 - sha512 pow
				int authTags = buf.readVarInt();
				if (authTags <= 0 || authTags > 0b111) {
					this.client.setScreen(new DisconnectedScreen(this.parentScreen, Text.of("OCAIP Disconnect"), Text.of("Server requested unknown authentication type")));
					return;
				}
				if (Tape.auth == null) {
					this.connection.handleDisconnection();
					if (this.connection.getAddress() instanceof InetSocketAddress) {
						this.client.setScreen(new AuthScreen(
								(InetSocketAddress)this.connection.getAddress(),
								this.parentScreen,
								(authTags & 0b1) != 0,
								(authTags & 0b10) != 0 ? buf.readString() : null,
								(authTags & 0b100) != 0 ? buf.readString() : null
						));
					} else {
						client.setScreen(this.parentScreen);
					}
					return;
				}
				if ((authTags & 0b1) != 0) {
					if (Tape.auth.pass == null) {
						this.client.setScreen(new DisconnectedScreen(this.parentScreen, Text.of("OCAIP Disconnect"), Text.of("No password was entered when it's required")));
						return;
					}
					tbuf.writeString(Tape.auth.pass);
				}
				if ((authTags & 0b10) != 0) {
					if (Tape.auth.pow == null) {
						this.client.setScreen(new DisconnectedScreen(this.parentScreen, Text.of("OCAIP Disconnect"), Text.of("No pow was generated when it's required")));
						return;
					}
					tbuf.writeString(Tape.auth.pow);
				}
				if ((authTags & 0b100) != 0) {
					if (Tape.auth.pow512 == null) {
						this.client.setScreen(new DisconnectedScreen(this.parentScreen, Text.of("OCAIP Disconnect"), Text.of("No sha512 pow was generated when it's required")));
						return;
					}
					tbuf.writeString(Tape.auth.pow512);
				}
			}
			Tape.auth = null;
			this.connection.send(new LoginQueryResponseC2SPacket(packet.getQueryId(), tbuf));
		}
	}

}