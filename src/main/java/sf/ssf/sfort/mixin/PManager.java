package sf.ssf.sfort.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class PManager {
    @Shadow
    private ServerPlayerEntity player;
    @Final @Shadow
    public ClientConnection connection;
    @Inject(method= "acceptPlayer()V", at=@At("TAIL"))
    public void createPlayer(CallbackInfo info){
        try {
            connection.acceptInboundMessage();
            connection.send(new CustomPayloadS2CPacket(new Identifier("OCAIP","reqPubEd"), new PacketByteBuf(Unpooled.buffer())));
        }catch (Exception ignored){}
        UUID uUID = PlayerEntity.getUuidFromProfile(profile);
        ServerLoginNetworkHandler
    }
}