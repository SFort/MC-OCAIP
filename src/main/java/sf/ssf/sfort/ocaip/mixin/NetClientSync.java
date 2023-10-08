package sf.ssf.sfort.ocaip.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sf.ssf.sfort.ocaip.OldCustomPayload;
import sf.ssf.sfort.ocaip.Reel;
import sf.ssf.sfort.ocaip.Tape;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class NetClientSync {


	@Shadow public abstract ClientConnection getConnection();

	@Inject(at=@At("TAIL"), method="onGameJoin(Lnet/minecraft/network/packet/s2c/play/GameJoinS2CPacket;)V")
	public void informAboutLocalSkins(CallbackInfo ci) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeMap(Reel.uuidToSkinHash, PacketByteBuf::writeString, PacketByteBuf::writeByteArray);
		getConnection().send(new CustomPayloadC2SPacket(new OldCustomPayload.Custom(new Identifier("ocaip", "local_skins"), buf)));
	}

	@Inject(at=@At("HEAD"), method= "onCustomPayload(Lnet/minecraft/network/packet/CustomPayload;)V", cancellable=true)
	public void ocaipCustomPacket(CustomPayload payload, CallbackInfo ci) {
		if (!(payload instanceof OldCustomPayload)) return;
		OldCustomPayload packet = (OldCustomPayload) payload;

		ci.cancel();
		PacketByteBuf buf = packet.buf;
		String path = packet.id.getPath();
		switch (path) {
			case "new_skins": {
				for (int size = buf.readVarInt(), i = 0; i < size; i++) {
					String uuid = buf.readString();
					byte[] file = buf.readByteArray();
					try {
						Reel.writeSkin(uuid, file);
					} catch (NoSuchAlgorithmException | IOException e) {
						Reel.log.error("could not save skin sent by serser", e);
					}
				}
				break;
			}
			case "request_skin": {
				if (Tape.localSkin != null) {
					getConnection().send(new CustomPayloadC2SPacket(new OldCustomPayload.Custom(new Identifier("ocaip", "set_skin"), new PacketByteBuf(Unpooled.buffer()).writeByteArray(Tape.localSkin))));
				}
				break;
			}
		}

	}

}