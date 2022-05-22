package sf.ssf.sfort.ocaip.nil;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.minecraft.client.network.ClientLoginNetworkHandler;
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
import nilloader.api.lib.asm.Opcodes;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.asm.tree.MethodInsnNode;
import nilloader.api.lib.mini.MiniTransformer;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import sf.ssf.sfort.ocaip.Reel;
import sf.ssf.sfort.ocaip.Wire;

import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import java.util.UUID;

@Patch.Class("net.minecraft.server.network.ServerLoginNetworkHandler")
public class NetServerLogin extends MiniTransformer {
	@Patch.Method("onHello(Lnet/minecraft/network/packet/c2s/login/LoginHelloC2SPacket;)V")
	public void patchAnnounce(PatchContext ctx){
		ctx.jumpToStart();
		ctx.add(
				ALOAD(0),
				ALOAD(1),
				ALOAD(0),
				GETFIELD("net/minecraft/client/network/ClientLoginNetworkHandler", "connection", "Lnet/minecraft/network/ClientConnection;"),
				INVOKESTATIC("sf/ssf/sfort/ocaip/nil/NetServerLogin$Hooks", "submitAuthRequest", "(Lnet/minecraft/network/packet/c2s/login/LoginHelloC2SPacket;Lnet/minecraft/network/ClientConnection;)[B"),
				PUTFIELD("net/minecraft/server/network/ServerLoginNetworkHandler", "ocaip$sentBytes", "[B")
		);
	}
	@Patch.Method("onKey(Lnet/minecraft/network/packet/c2s/login/LoginKeyC2SPacket;)V")
	public void patchBypass(PatchContext ctx){
		ctx.jumpToStart();
		ctx.search(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Thread", "start", "()V")).jumpBefore();
		LabelNode LABEL = new LabelNode();
		ctx.add(
				ALOAD(0),
				INVOKESTATIC("sf/ssf/sfort/ocaip/nil/NetServerLogin$Hooks", "bypassAuthPacket", "(Lnet/minecraft/server/network/ServerLoginNetworkHandler;)Z"),
				IFEQ(LABEL),
				RETURN(),
				LABEL
		);
	}
	@Patch.Method("onQueryResponse(Lnet/minecraft/network/packet/c2s/login/LoginQueryResponseC2SPacket;)V")
	public void patchCustomPacket(PatchContext ctx){
		ctx.jumpToStart();
		LabelNode LABEL = new LabelNode();
		ctx.add(
				ALOAD(0),
				ALOAD(1),
				INVOKESTATIC("sf/ssf/sfort/ocaip/nil/NetServerLogin$Hooks", "handleCustomPacket", "(Lnet/minecraft/network/packet/c2s/login/LoginQueryResponseC2SPacket;Lnet/minecraft/server/network/ServerLoginNetworkHandler;)Z"),
				IFEQ(LABEL),
				RETURN(),
				LABEL
		);
	}
	public static class Hooks {
		public static boolean handleCustomPacket(ServerLoginNetworkHandler self, LoginQueryResponseC2SPacket packet) {
			int pid = packet.getQueryId();
			if (pid == 41809950) {
				PacketByteBuf buf = packet.getResponse();
				if (buf == null) return true;
				try {
					Field field = ServerLoginNetworkHandler.class.getDeclaredField("ocaip$hasBypassed");
					field.setByte(self, (byte)(field.getByte(self) & 0b10));
				} catch (Exception e) {
					Reel.log.error("Failed to get hasBypassed filed", e);
					return false;
				}
				return true;
			} else if (pid == 41809951 || pid == 41809952) {
				PacketByteBuf buf = packet.getResponse();
				if (ocaip$sentBytes == null || buf == null) {
					return true;
				}
				int version = buf.readVarInt();
				byte[] pubKeyRecv = buf.readByteArray();
				String name;
				try {
					Field profileField = ServerLoginNetworkHandler.class.getDeclaredField("profile");
					profileField.setAccessible(true);
					name = ((GameProfile) profileField.get(self)).getName();
				} catch (Exception e) {
					Reel.log.error("Failed to get profile filed", e);
					return false;
				}
				byte[] recvBytes = buf.readByteArray();
				PublicKey pubKey = Wire.keys.get(name);
				EdDSAEngine engine = new EdDSAEngine();
				PublicKey recvKey;
				try {
					recvKey = new EdDSAPublicKey(new X509EncodedKeySpec(pubKeyRecv));
				} catch (Exception ignore) {
					self.disconnect(new LiteralText("OCAIP: Failed to read public key"));
					return true;
				}
				if (pubKey != null) {
					if (pubKey.hashCode() != recvKey.hashCode()) {
						self.disconnect(new LiteralText("OCAIP: Key already exists for this user, change username or contact admin"));
						return true;
					}
				} else if (Wire.keys.containsKey(name)) {
					self.disconnect(new LiteralText("OCAIP: Key already exists for this user, change username or contact admin"));
					return true;
				} else {
					if (Wire.password != null){
						String recvPass = buf.readString();
						if (!Wire.password.equals(recvPass)) {
							self.disconnect(new LiteralText("OCAIP: Wrong Password"));
							return true;
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
						self.disconnect(new LiteralText("OCAIP: Signature invalid for sent bytes"));
						return true;
					}
				} catch (SignatureException exception) {
					self.disconnect(new LiteralText("OCAIP: Got invalid sig"));
					return true;
				} catch (InvalidKeyException exception) {
					self.disconnect(new LiteralText("OCAIP: Got invalid key"));
					return true;
				}
				Reel.log.info("Username "+name+" logged in");
				try {
					Field field = ServerLoginNetworkHandler.class.getDeclaredField("ocaip$hasBypassed");
					field.setByte(self, (byte)(field.getByte(self) | 0b10));
				} catch (Exception e) {
					Reel.log.error("Failed to get hasBypassed filed", e);
					return false;
				}
				return true;
			}
			return false;
		}
		public static boolean bypassAuthPacket(ServerLoginNetworkHandler self) {
			GameProfile profile;
			try {
				Field profileField = ServerLoginNetworkHandler.class.getDeclaredField("profile");
				profileField.setAccessible(true);
				profile = (GameProfile) profileField.get(self);
				profileField.set(self, new GameProfile(PlayerEntity.getOfflinePlayerUuid(profile.getName()), profile.getName()));
			} catch (Exception e) {
				Reel.log.error("Failed to get profile filed", e);
				return false;
			}
			byte hasBypass;
			try {
				hasBypass = ServerLoginNetworkHandler.class.getDeclaredField("ocaip$hasBypassed").getByte(self);
			} catch (Exception e) {
				Reel.log.error("Failed to get hasBypassed filed", e);
				return false;
			}

			if (hasBypass == 3) {
				try {
					Field stateField = ServerLoginNetworkHandler.class.getDeclaredField("state");
					stateField.setAccessible(true);
					//READY_TO_ACCEPT
					stateField.set(self, stateField.getType().getEnumConstants()[4]);
				} catch (Exception e) {
					Reel.log.error("Failed to get state filed", e);
					return false;
				}
				return true;
			} else try {
				Wire.addAndWrite(profile.getName(), null);
			} catch (Exception ignore) {}
			return false;
		}

		public static byte[] submitAuthRequest(LoginHelloC2SPacket packet, ClientConnection connection) {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeVarInt(Reel.protocalVersion);
			byte[] bytes = new byte[256];
			try {
				Field profileField = ServerLoginNetworkHandler.class.getDeclaredField("RANDOM");
				profileField.setAccessible(true);
				((Random) profileField.get(null)).nextBytes(bytes);
			} catch (Exception e) {
				Reel.log.error("Failed to get RANDOM filed", e);
				return null;
			}
			buf.writeByteArray(bytes);
			connection.send(new LoginQueryRequestS2CPacket(
					Wire.password == null || Wire.keys.containsKey(packet.getProfile().getName()) ? 41809951 : 41809952,
					new Identifier("ocaip", "request_auth"),
					buf));
			//ocaip$sentBytes
			return bytes;
		}
	}

}