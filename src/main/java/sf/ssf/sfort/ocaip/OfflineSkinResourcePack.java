package sf.ssf.sfort.ocaip;

import com.google.common.collect.ImmutableSet;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class OfflineSkinResourcePack implements ResourcePack {
	@Nullable
	@Override
	public InputStream openRoot(String fileName) throws IOException {
		if (fileName.contains("/") || fileName.contains("\\")) {
			throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
		}
		return getIS(fileName);
	}

	public static byte[] getPngBytes(String name) {
		try {
			return Files.readAllBytes(new File(Reel.skinDir+"/"+name+".png").toPath());
		} catch (IOException e) {
			Reel.log.error("Failed to read png: "+name, e);
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
	public InputStream open(ResourceType type, Identifier id) throws IOException {
		if (type!= ResourceType.CLIENT_RESOURCES) throw new FileNotFoundException(id.toString());
		return getIS(id.getPath()+".png");
	}

	@Override
	public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
		return Collections.emptySet();
	}

	@Override
	public boolean contains(ResourceType type, Identifier id) {
		try {
			getIS(id.getPath()+".png");
		} catch (IOException e) {
			return false;
		}
		return true;
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
