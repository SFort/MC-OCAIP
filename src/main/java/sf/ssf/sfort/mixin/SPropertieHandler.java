package sf.ssf.sfort.mixin;

import net.minecraft.server.dedicated.ServerPropertiesHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPropertiesHandler.class)
public abstract class SPropertieHandler {
	@Final @Shadow
	public final boolean onlineMode = false;
}
