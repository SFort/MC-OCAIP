package sf.ssf.sfort.ocaip.mixin;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import io.netty.buffer.Unpooled;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.minecraft.client.MinecraftClient;
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
import sf.ssf.sfort.ocaip.OCAIPPassworded;
import sf.ssf.sfort.ocaip.PasswordScreen;
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
			PacketByteBuf tbuf = new PacketByteBuf(Unpooled.buffer()).writeVarInt(Reel.protocalVersion).writeByteArray(Tape.key.getPublic().getEncoded()).writeByteArray(bytes);
			if (pid == 41809952) {
				if (!(this.client.currentScreen instanceof OCAIPPassworded)){
					this.connection.handleDisconnection();
					this.client.setScreen(parentScreen);
					return;
				}
				String pass = ((OCAIPPassworded)this.client.currentScreen).ocaip$getPassword();
				if (pass == null) {
					this.connection.handleDisconnection();
					if (this.connection.getAddress() instanceof InetSocketAddress) {
						this.client.setScreen(new PasswordScreen((InetSocketAddress)this.connection.getAddress(), this.parentScreen));
					} else {
						client.setScreen(this.parentScreen);
					}
					return;
				}
				tbuf.writeString(pass);
			}
			this.connection.send(new LoginQueryResponseC2SPacket(packet.getQueryId(), tbuf));
			ci.cancel();

		}
	}

}