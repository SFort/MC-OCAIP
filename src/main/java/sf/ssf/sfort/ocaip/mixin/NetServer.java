package sf.ssf.sfort.ocaip.mixin;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class NetServer {
	@Shadow
	public ServerPlayerEntity player;
	@Final @Shadow
	private ClientConnection connection;/*
	@Inject(at=@At("HEAD"), method= "onCustomPayload(Lnet/minecraft/network/packet/c2s/play/CustomPayloadC2SPacket;)V", cancellable=true)
	public void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		if (packet.getChannel().getNamespace().equals("OCAIP")) {
			switch (packet.getChannel().getPath()){
				case "PubEd" : break;
			}
			new EdDSAEngine();
			//connection.send(new CustomPayloadS2CPacket(new Identifier("OCAIP","reqPubEd"), new PacketByteBuf(Unpooled.buffer())));
			PacketByteBuf buf = packet.getData();
			int bits = buf.readVarInt();
			ci.cancel();
		}
	}*/
}