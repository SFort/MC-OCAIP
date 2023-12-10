package sf.ssf.sfort.ocaip.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.Config;
import sf.ssf.sfort.ocaip.Reel;
import sf.ssf.sfort.ocaip.Wire;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

//From the bottom of my heart the fabricAPI dev that made these bits if you're reading this never code again, thanks.
public class MixinConfigPlugin implements IMixinConfigPlugin {

	public static Function<Object, List<IMixinConfig>> getMixinInfoFromClassInfo = i -> Collections.emptyList();
	static {
		try {
			Class<?> transClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");
			Class<?> procClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinProcessor");
			Field processorField = transClass.getDeclaredField("processor");
			Field pendingField = procClass.getDeclaredField("pendingConfigs");
			processorField.setAccessible(true);
			pendingField.setAccessible(true);
			getMixinInfoFromClassInfo = tr -> {
				try {
					if (transClass.isInstance(tr)) {
						Object proc = processorField.get(tr);
						if (procClass.isInstance(proc)) {
							Object pend = pendingField.get(proc);
							if (pend instanceof List) {
								List<?> list = (List<?>) pend;
								List<IMixinConfig> ret = new ArrayList<>(list.size());
								for (Object o : list) {
									if (o instanceof IMixinConfig) {
										ret.add((IMixinConfig) o);
									}
								}
								return ret;
							}
						}
					}
				} catch (Exception e) {
					Reel.log.error("Failed to reflect fabric-networking, vanilla auth likely broken: ", e);
				}
				return Collections.emptyList();
			};
		} catch (Throwable e) {
			Reel.log.error("Failed to reflect fabric-networking, vanilla auth likely broken: ", e);
		}
	}

	@Override
	public void onLoad(String mixinPackage) {
		if  (!Wire.disableFAPIjank) return;
		for (Config config : Mixins.getConfigs()) {
			processIMC(config.getConfig());
		}
		for (IMixinConfig imc : getMixinInfoFromClassInfo.apply(MixinEnvironment.getCurrentEnvironment().getActiveTransformer())) {
			processIMC(imc);
		}

	}
	public static void processIMC(IMixinConfig imc) {
		if (imc == null) return;
		if (imc.getMixinPackage().startsWith("net.fabricmc.fabric.mixin.networking")) {
			List<String> mixinClassesClient = pluck(imc.getClass(), imc, "mixinClasses");
			if (mixinClassesClient == null) return;
			if (mixinClassesClient.remove("ServerLoginNetworkHandlerMixin")) {
				Reel.log.warn("REMOVING FAPIs ServerLoginNetworkHandlerMixin as it is soo poorly made it shouldn't exist, this may break some login networking, feel free to discuss this at https://github.com/FabricMC/fabric ");
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T pluck(Class<?> clazz, Object inst, String field) {
		Field f = null;
		Class<?> cursor = clazz;
		while (f == null && cursor != null) {
			try {
				f = cursor.getDeclaredField(field);
			} catch (NoSuchFieldException ignore) {}
			cursor = cursor.getSuperclass();
		}
		if (f == null) throw new NoSuchFieldError(field);
		f.setAccessible(true);
		try {
			return (T)f.get(inst);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

}

