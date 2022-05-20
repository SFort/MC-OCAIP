package sf.ssf.sfort.ocaip.nil;

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
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.MiniTransformer;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import sf.ssf.sfort.ocaip.OCAIPPassworded;
import sf.ssf.sfort.ocaip.PasswordScreen;
import sf.ssf.sfort.ocaip.Reel;
import sf.ssf.sfort.ocaip.Tape;

import java.net.InetSocketAddress;

@Patch.Class("net.minecraft.client.network.ClientLoginNetworkHandler")
public abstract class NetClientLogin extends MiniTransformer {

	@Patch.Method("onQueryRequest(Lnet/minecraft/network/packet/s2c/login/LoginQueryRequestS2CPacket;)V")
	public void patchQuery(PatchContext ctx){
		ctx.jumpToStart();
		LabelNode LABEL = new LabelNode();
		ctx.add(
				ALOAD(0),
				DUP(),
				GETFIELD("net/minecraft/client/network/ClientLoginNetworkHandler", "client","net/minecraft/client/MinecraftClient"),
				ALOAD(0),
				GETFIELD("net/minecraft/client/network/ClientLoginNetworkHandler", "connection", "net/minecraft/network/ClientConnection"),
				ALOAD(0),
				GETFIELD("net/minecraft/client/network/ClientLoginNetworkHandler", "parentScreen", "net/minecraft/client/gui/screen/Screen"),
				ALOAD(1),
				INVOKESTATIC("sf/ssf/sfort/ocaip/nil/NetClientLogin$Hooks", "bypassAuthPacket", "(Lnet/minecraft/client/network/ClientLoginNetworkHandler;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/network/ClientConnection;Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/network/packet/s2c/login/LoginQueryRequestS2CPacket;)Z"),
				IFEQ(LABEL),
				RETURN(),
				LABEL

		);
	}
	@Patch.Method("joinServerSession(Ljava/lang/String;)Lnet/minecraft/text/Text;")
	public void patchYgg(PatchContext ctx){
		ctx.jumpToStart();
		PatchContext.SearchResult result = ctx.search(ARETURN());
		while (result.isSuccessful()) {
			result.jumpBefore();
			LabelNode LABEL = new LabelNode();
			ctx.add(
					ALOAD(0),
					GETFIELD("net/minecraft/client/network/ClientLoginNetworkHandler", "ocaip$recivedRequest", "Z"),
					IFNE(LABEL),
					ALOAD(0),
					GETFIELD("net/minecraft/client/network/ClientLoginNetworkHandler", "connection", "net/minecraft/network/ClientConnection"),
					INVOKESTATIC("sf/ssf/sfort/ocaip/nil/NetClientLogin$Hooks", "ignoreYggdrasilErr", "(Lnet/minecraft/text/Text;Lnet/minecraft/network/ClientConnection;)Lnet/minecraft/text/Text;"),
					LABEL
					);
			result.next();
		}

	}

	public static class Hooks {

		public Text ignoreYggdrasilErr(Text text, ClientConnection connection) {
			if (text == null) {
				connection.send(new LoginQueryResponseC2SPacket(41809950, new PacketByteBuf(Unpooled.buffer()).writeVarInt(Reel.protocalVersion)));
			}
			return null;
		}

		public static boolean bypassAuthPacket(ClientLoginNetworkHandler self, MinecraftClient client, ClientConnection connection, Screen parentScreen, LoginQueryRequestS2CPacket packet) {
			int pid = packet.getQueryId();
			if (pid == 41809951 || pid == 41809952) {
				try {
					ClientLoginNetworkHandler.class.getField("ocaip$recivedRequest").setBoolean(self, true);
				} catch (Exception e) {
					Reel.log.error("Failed to get recivedRequest filed", e);
					return false;
				}
				PacketByteBuf buf = packet.getPayload();
				if (buf == null) return false;
				int version = buf.readVarInt();
				byte[] bytes = buf.readByteArray();
				EdDSAEngine engine = new EdDSAEngine();
				try {
					engine.initSign(Tape.key.getPrivate());
					bytes = engine.signOneShot(bytes);
				} catch (Exception ignore) {
					return false;
				}
				PacketByteBuf tbuf = new PacketByteBuf(Unpooled.buffer()).writeVarInt(Reel.protocalVersion).writeByteArray(Tape.key.getPublic().getEncoded()).writeByteArray(bytes);
				if (pid == 41809952) {
					if (!(client.currentScreen instanceof OCAIPPassworded)) {
						connection.handleDisconnection();
						client.setScreen(parentScreen);
						return false;
					}
					String pass = ((OCAIPPassworded) client.currentScreen).ocaip$getPassword();
					if (pass == null) {
						connection.handleDisconnection();
						if (connection.getAddress() instanceof InetSocketAddress) {
							client.setScreen(new PasswordScreen((InetSocketAddress) connection.getAddress(), parentScreen));
						} else {
							client.setScreen(parentScreen);
						}
						return false;
					}
					tbuf.writeString(pass);
				}
				connection.send(new LoginQueryResponseC2SPacket(packet.getQueryId(), tbuf));
				return true;
			}
			return false;
		}
	}
}