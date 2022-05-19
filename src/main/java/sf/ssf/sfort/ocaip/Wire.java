package sf.ssf.sfort.ocaip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.i2p.crypto.eddsa.EdDSAPublicKey;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Environment(EnvType.SERVER)
public class Wire {
	public static final File conf = new File("ocaip/server_keys");

	public static Map<String, PublicKey> keys = new HashMap<>();
	public static String password = null;

	public static void addAndWrite(String name, PublicKey key) throws IOException {
		if (!keys.containsKey(name)) {
			keys.put(name, key);
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, PublicKey> entry : keys.entrySet()) {
				sb.append(entry.getKey()).append('\n');
				if (entry.getValue() != null)
					sb.append(Base64.getEncoder().encodeToString(entry.getValue().getEncoded()));
				sb.append('\n');
			}
			Files.writeString(conf.toPath(), sb);
		}
	}

	static {
		Reel.createDir();
		try {
			String s = Files.readString(new File("ocaip/password").toPath()).trim();
			if (!s.isBlank()) password = s;
		} catch (Exception ignore) {
		}
		try {
			List<String> ln = Files.readAllLines(conf.toPath());
			for (int i = 1; i <= ln.size() / 2; i += 2) {
				String s = ln.get(i);
				keys.put(ln.get(i - 1), s.isBlank() ? null : new EdDSAPublicKey(new X509EncodedKeySpec(Base64.getDecoder().decode(s))));
			}
		} catch (FileSystemException e) {
			try {
				conf.createNewFile();
			} catch (Exception ignore) {
			}
		} catch (Exception ignore) {
		}
	}
}
