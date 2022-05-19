package sf.ssf.sfort.ocaip.mixin;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sf.ssf.sfort.ocaip.Reel;
import sf.ssf.sfort.ocaip.Wire;

import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import java.util.UUID;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class NetServerLogin {

	boolean ocaip$hasBypassed = false;
    boolean ocaip$shouldBypass = true;
	byte[] ocaip$sentBytes = null;

	@Shadow
	ServerLoginNetworkHandler.State state;
	@Shadow @Final
	public ClientConnection connection;
	@Shadow
	GameProfile profile;
	@Shadow
	protected abstract GameProfile toOfflineProfile(GameProfile profile);
	@Shadow
	public abstract void disconnect(Text reason);
	@Shadow @Final
	private static Random RANDOM;

	@Inject(at=@At("HEAD"), method="onHello(Lnet/minecraft/network/packet/c2s/login/LoginHelloC2SPacket;)V")
	public void submitAuthRequest(LoginHelloC2SPacket packet, CallbackInfo ci) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeVarInt(Reel.protocalVersion);
		byte[] bytes = new byte[256];
		RANDOM.nextBytes(bytes);
		buf.writeByteArray(bytes);
		connection.send(new LoginQueryRequestS2CPacket(
				Wire.password == null || Wire.keys.containsKey(packet.getProfile().getName()) ? 41809951 : 41809952,
				new Identifier("ocaip", "request_auth"),
				buf));
		ocaip$sentBytes = bytes;
	}

	@Inject(at = @At(value="INVOKE", target="Ljava/lang/Thread;start()V", shift=At.Shift.BEFORE), method="onKey(Lnet/minecraft/network/packet/c2s/login/LoginKeyC2SPacket;)V", cancellable=true)
	public void bypassAuthPacket(CallbackInfo ci) {
		this.profile = this.toOfflineProfile(this.profile);
		if (ocaip$hasBypassed && ocaip$shouldBypass) {
			this.state = ServerLoginNetworkHandler.State.READY_TO_ACCEPT;
			ci.cancel();
		} else try {
			Wire.addAndWrite(profile.getName(), null);
		} catch (Exception ignore) {}
	}

	@Inject(at = @At("HEAD"), method="onQueryResponse(Lnet/minecraft/network/packet/c2s/login/LoginQueryResponseC2SPacket;)V", cancellable=true)
	public void bypassAuthPacket(LoginQueryResponseC2SPacket packet, CallbackInfo ci) {
		int pid = packet.getQueryId();
        if (pid == 41809950) {
            ci.cancel();
            PacketByteBuf buf = packet.getResponse();
            if (buf == null) return;
            ocaip$shouldBypass = false;
        } else if (pid == 41809951 || pid == 41809952) {
			ci.cancel();
			PacketByteBuf buf = packet.getResponse();
			if (ocaip$sentBytes == null || buf == null) {
				return;
			}
			int version = buf.readVarInt();
			byte[] pubKeyRecv = buf.readByteArray();
			String name = profile.getName();
			byte[] recvBytes = buf.readByteArray();
			PublicKey pubKey = Wire.keys.get(name);
			EdDSAEngine engine = new EdDSAEngine();
			PublicKey recvKey;
			try {
				recvKey = new EdDSAPublicKey(new X509EncodedKeySpec(pubKeyRecv));
			} catch (Exception ignore) {
				this.disconnect(new LiteralText("OCAIP: Failed to read public key"));
				return;
			}
			if (pubKey != null) {
				if (pubKey.hashCode() != recvKey.hashCode()) {
					this.disconnect(new LiteralText("OCAIP: Key already exists for this user, change username or contact admin"));
					return;
				}
			} else if (Wire.keys.containsKey(name)) {
				this.disconnect(new LiteralText("OCAIP: Key already exists for this user, change username or contact admin"));
				return;
			} else {
				if (Wire.password != null){
					String recvPass = buf.readString();
					if (!Wire.password.equals(recvPass)) {
						this.disconnect(new LiteralText("OCAIP: Wrong Password"));
						return;
					}
				}
				try {
					Wire.addAndWrite(name, recvKey);
				} catch (Exception e) {
					Reel.log.error("Failed to save new user", e);
				}
			}
			try {
				engine.initVerify(recvKey);
				if (!engine.verifyOneShot(ocaip$sentBytes, recvBytes)) {
					this.disconnect(new LiteralText("OCAIP: Signature invalid for sent bytes"));
					return;
				}
			} catch (SignatureException exception) {
				this.disconnect(new LiteralText("OCAIP: Got invalid sig"));
				return;
			} catch (InvalidKeyException exception) {
				this.disconnect(new LiteralText("OCAIP: Got invalid key"));
				return;
			}
			Reel.log.info("Username "+name+" logged in");
			ocaip$hasBypassed = true;
		}
	}

}