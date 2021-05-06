package sf.ssf.sfort.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftClient.class)
public abstract class ServerDedi {
    ServerPlayerEntity
    /*
    YggdrasilAuthenticationService;
    YggdrasilMinecraftSessionService;
    GameProfile;
    MinecraftClient;
    */
}