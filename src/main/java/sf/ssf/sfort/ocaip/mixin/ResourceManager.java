package sf.ssf.sfort.ocaip.mixin;

import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.resource.ResourceType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sf.ssf.sfort.ocaip.OfflineSkinResourcePack;

import java.util.List;
import java.util.Map;

@Mixin(LifecycledResourceManagerImpl.class)
public class ResourceManager {
	@Shadow @Final private Map<String, NamespaceResourceManager> subManagers;

	@Inject(method="<init>(Lnet/minecraft/resource/ResourceType;Ljava/util/List;)V", at=@At("TAIL"))
	private void addSkins(ResourceType type, List packs, CallbackInfo ci) {
		if (type != ResourceType.CLIENT_RESOURCES) return;
		this.subManagers.computeIfAbsent("ocaip_skin", namespace -> new NamespaceResourceManager(type, namespace)).addPack(new OfflineSkinResourcePack());
	}
}
