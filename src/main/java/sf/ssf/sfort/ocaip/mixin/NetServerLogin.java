package sf.ssf.sfort.ocaip.mixin;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponsePayload;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sf.ssf.sfort.ocaip.OldCustomPayload;
import sf.ssf.sfort.ocaip.Reel;
import sf.ssf.sfort.ocaip.Wire;

import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class NetServerLogin {

	boolean ocaip$hasBypassed = false;
    boolean ocaip$shouldBypass = true;
	byte[] ocaip$sentBytes = null;

	@Shadow @Final
	public ClientConnection connection;
	@Shadow
	GameProfile profile;
	@Shadow
	public abstract void disconnect(Text reason);

	@Shadow
	protected static GameProfile createOfflineProfile(String name) {
		return null;
	}

	@Shadow protected abstract void sendSuccessPacket(GameProfile profile);

	@Shadow private @Nullable String profileName;
	private static final Random ocaip$random = new Random();

	@Inject(at=@At("HEAD"), method="onHello(Lnet/minecraft/network/packet/c2s/login/LoginHelloC2SPacket;)V")
	public void submitAuthRequest(LoginHelloC2SPacket packet, CallbackInfo ci) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeVarInt(Reel.protocolVersion);
		byte[] bytes = new byte[256];
		ocaip$random.nextBytes(bytes);
		buf.writeByteArray(bytes);
		String name = packet.name();
		int requestCode = Wire.keys.containsKey(name) || (Wire.password == null && Wire.pow == null) ? 41809951 : 41809952;
		if (requestCode == 41809952) {
			buf.writeVarInt((Wire.password == null ? 0 : 0b1) | (Wire.pow == null ? 0 : 0b10));
			if (Wire.pow != null) {
				String ip = connection.getAddress().toString();
				int i = ip.lastIndexOf(':');
				if (i!=-1) ip = ip.substring(0, i);
				buf.writeString(Wire.pow.genPrompt(name+ip, ocaip$random));
			}
		}
		connection.send(new LoginQueryRequestS2CPacket(
				requestCode, new OldCustomPayload(
				new Identifier("ocaip", "request_auth"),
				buf)));
		ocaip$sentBytes = bytes;
	}

	@Inject(at = @At(value="INVOKE", target="Ljava/lang/Thread;start()V", shift=At.Shift.BEFORE), method="onKey(Lnet/minecraft/network/packet/c2s/login/LoginKeyC2SPacket;)V", cancellable=true)
	public void bypassAuthPacket(CallbackInfo ci) {
		this.profile = createOfflineProfile(this.profileName);
		if (ocaip$hasBypassed && ocaip$shouldBypass) {
			sendSuccessPacket(profile);
			ci.cancel();
		} else try {
			if (Wire.requireOCAIP) {
				disconnect(Text.literal("OCAIP: Server requires OCAIP auth"));
				ci.cancel();
				return;
			}
			Wire.addAndWrite(profile.getName(), null);
		} catch (Exception ignore) {}
	}

	@Inject(at = @At("HEAD"), method="onQueryResponse(Lnet/minecraft/network/packet/c2s/login/LoginQueryResponseC2SPacket;)V", cancellable=true)
	public void bypassAuthPacket(LoginQueryResponseC2SPacket inPayload, CallbackInfo ci) {
		int pid = inPayload.queryId();
		LoginQueryResponsePayload payload = inPayload.response();
		if (!(payload instanceof OldCustomPayload)) return;
		OldCustomPayload packet = ((OldCustomPayload) payload);
        if (pid == 41809950) {
            ci.cancel();
            PacketByteBuf buf = packet.buf;
            if (buf == null) return;
			if (Wire.requireOCAIP) return;
			ocaip$shouldBypass = false;
        } else if (pid == 41809951 || pid == 41809952) {
			ci.cancel();
			PacketByteBuf buf = packet.buf;
			if (ocaip$sentBytes == null || buf == null) {
				return;
			}
			int version = buf.readVarInt();
			if (pid != 41809951 && version <= 1) {
				this.disconnect(Text.of("Client running an incompatible version of OCAIP"));
				return;
			}
			byte[] pubKeyRecv = buf.readByteArray();
			String name = this.profileName;
			byte[] recvBytes = buf.readByteArray();
			PublicKey pubKey = Wire.keys.get(name);
			EdDSAEngine engine = new EdDSAEngine();
			PublicKey recvKey;
			try {
				recvKey = new EdDSAPublicKey(new X509EncodedKeySpec(pubKeyRecv));
			} catch (Exception ignore) {
				this.disconnect(Text.literal("OCAIP: Failed to read public key"));
				return;
			}
			if (pubKey != null) {
				if (pubKey.hashCode() != recvKey.hashCode()) {
					this.disconnect(Text.literal("OCAIP: Key already exists for this user, change username or contact admin"));
					return;
				}
			} else if (Wire.keys.containsKey(name)) {
				this.disconnect(Text.literal("OCAIP: Key already exists for this user, change username or contact admin"));
				return;
			} else {
				String recvPass = Wire.password == null ? null : buf.readString();
				if (Wire.pow != null) {
					String ip = connection.getAddress().toString();
					int i = ip.lastIndexOf(':');
					if (i!=-1) ip = ip.substring(0, i);
					ip = name + ip;
					if (!Wire.pow.sessionQueries.containsKey(ip)) {
						this.disconnect(Text.literal("OCAIP: Server doesn't remember prompting sha1 proof of work"));
						return;
					}
					if (!Wire.pow.isResponseValid(ip, buf.readString())) {
						this.disconnect(Text.literal("OCAIP: Invalid sha1 proof of work"));
						return;
					}
				}
				if (Wire.password != null){
					if (!Wire.password.equals(recvPass)) {
						this.disconnect(Text.literal("OCAIP: Wrong Password"));
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
					this.disconnect(Text.literal("OCAIP: Signature invalid for sent bytes"));
					return;
				}
			} catch (SignatureException exception) {
				this.disconnect(Text.literal("OCAIP: Got invalid sig"));
				return;
			} catch (InvalidKeyException exception) {
				this.disconnect(Text.literal("OCAIP: Got invalid key"));
				return;
			}
			Reel.log.info("Username "+name+" logged in");
			ocaip$hasBypassed = true;
		}
	}

}