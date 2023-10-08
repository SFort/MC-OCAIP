package sf.ssf.sfort.ocaip.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sf.ssf.sfort.ocaip.OfflineSkinResourcePack;
import sf.ssf.sfort.ocaip.OldCustomPayload;
import sf.ssf.sfort.ocaip.Reel;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class NetServerSync {

	@Shadow @Final public ClientConnection connection;

	@Shadow @Final private MinecraftServer server;

	@Inject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/c2s/common/CustomPayloadC2SPacket;)V", cancellable=true)
	public void ocaipCustomPacket(CustomPayloadC2SPacket inPacket, CallbackInfo ci) {
		if (!(((Object)this) instanceof ServerPlayNetworkHandler)) return;
		CustomPayload payload = inPacket.payload();
		if (!(payload instanceof OldCustomPayload)) return;
		ServerPlayNetworkHandler self = (ServerPlayNetworkHandler) (Object) this;
		ServerPlayerEntity player = self.getPlayer();
		OldCustomPayload packet = (OldCustomPayload) payload;
		if (packet.id.getNamespace().equals("ocaip")) {
			ci.cancel();
			PacketByteBuf buf = packet.buf;
			String path = packet.id.getPath();
			switch (path) {
				case "local_skins": {
					Map<String, byte[]> recv = buf.readMap(PacketByteBuf::readString, PacketByteBuf::readByteArray);
					String uuid = player.getUuid().toString();
					{
						byte[] playerHash = recv.get(uuid);
						byte[] serverPlayerHash = Reel.uuidToSkinHash.get(uuid);
						if (playerHash != null && (serverPlayerHash == null || !Arrays.equals(serverPlayerHash, playerHash))) {
							this.connection.send(new CustomPayloadS2CPacket(new OldCustomPayload(new Identifier("ocaip", "request_skin"), new PacketByteBuf(Unpooled.buffer()).writeString(uuid))));
						}
					}
					Map<String, byte[]> mismatched = new HashMap<>();
					Reel.uuidToSkinHash.forEach((key, entry)->{
						if (key.equals(uuid)) return;
						byte[] arr = recv.get(key);
						if (arr != null && Arrays.equals(arr, entry)) {
							return;
						}
						byte[] tmp = OfflineSkinResourcePack.getPngBytes(key);
						if (tmp != null) mismatched.put(key, tmp);
					});
					if (!mismatched.isEmpty()) {
						buf = new PacketByteBuf(Unpooled.buffer());
						buf.writeMap(mismatched, PacketByteBuf::writeString, PacketByteBuf::writeByteArray);
						this.connection.send(new CustomPayloadS2CPacket(new OldCustomPayload(new Identifier("ocaip", "new_skins"), buf)));
					}
					break;
				}
				case "set_skin": {
					byte[] skin = buf.readByteArray(409600);
					String uuid = player.getUuid().toString();
					if (skin != null) {
						try {
							Reel.writeSkin(uuid, skin);
							buf = new PacketByteBuf(Unpooled.buffer());
							buf.writeMap(Map.of(uuid, skin), PacketByteBuf::writeString, PacketByteBuf::writeByteArray);
							CustomPayloadS2CPacket sendPckt = new CustomPayloadS2CPacket(new OldCustomPayload(new Identifier("ocaip", "new_skins"), buf));
							for (ServerPlayerEntity spe : this.server.getPlayerManager().getPlayerList()) {
								if (spe == player) continue;
								spe.networkHandler.sendPacket(sendPckt);
							}
						} catch (NoSuchAlgorithmException | IOException e) {
							Reel.log.error("failed to save skin for "+player.getName(), e);
						}
					}
					break;
				}
			}
		}
	}
}