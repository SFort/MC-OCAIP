package sf.ssf.sfort.ocaip;

import com.google.common.collect.ImmutableSet;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OfflineSkinResourcePack implements ResourcePack {
	@Nullable @Override
	public InputSupplier<InputStream> openRoot(String... segments) {
		if (segments.length == 0) return null;
		if (segments.length > 1) {
			throw new IllegalArgumentException("Too many segments");
		}
		String name = segments[0];
		return () -> getIS(name);
	}

	public static byte[] getPngBytes(String name) {
		try {
			return Files.readAllBytes(new File(Reel.skinDir+"/"+name+".png").toPath());
		} catch (IOException e) {
			Reel.log.warn("Failed to read png: "+name);
		}
		return null;
	}

	public static Map<String, Boolean> hasTextureCache = new HashMap<>();
	public static boolean hasIs(String name) {
		Boolean bl = hasTextureCache.get(name);
		if (bl != null) return bl;
		if (new File(Reel.skinDir+"/"+name+".png").isFile()) {
			hasTextureCache.put(name, true);
			return true;
		}
		hasTextureCache.put(name, false);
		return false;
	}

	public static InputStream getIS(String name) throws IOException {
		return Reel.stripAlpha(new FileInputStream(Reel.skinDir+"/"+name));
	}

	@Override
	public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
		if (type!= ResourceType.CLIENT_RESOURCES) return null;
		try {
			if (getIS(id.getPath() + ".png") == null) return null;
		} catch (Exception e) {
			return null;
		}
		return () -> getIS(id.getPath()+".png");
	}

	@Override
	public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {

	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		return ImmutableSet.of("ocaip_skin");
	}

	@Nullable
	@Override
	public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
		throw new FileNotFoundException("no metadata exists");
	}

	@Override
	public String getName() {
		return "OCAIP Skins";
	}

	@Override
	public void close() {

	}
}
