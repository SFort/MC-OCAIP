package sf.ssf.sfort.ocaip.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class NetClient {
	@Final @Shadow
	private ClientConnection connection;

	@Inject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/s2c/play/CustomPayloadS2CPacket;)V", cancellable=true)
	public void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		if (packet.getChannel().getNamespace().equals("OCAIP")) {
			switch (packet.getChannel().getPath()){
				case "reqPubEd" : break;
				default : return;
			}
			//connection.send(new CustomPayloadS2CPacket(new Identifier("OCAIP","reqPubEd"), new PacketByteBuf(Unpooled.buffer())));
			PacketByteBuf buf = packet.getData();
			int bits = buf.readVarInt();
			ci.cancel();
		}
	}
}