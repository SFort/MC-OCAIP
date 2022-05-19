package sf.ssf.sfort.ocaip;

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
import java.util.UUID;

public class Wire {
	public static final File conf = new File("OCAIP.keys");
	public static Map<UUID, PublicKey> keys = new HashMap<>();
	public static String password = null;

	public static void addAndWrite(UUID uuid, PublicKey key) throws IOException {
		keys.put(uuid, key);
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<UUID, PublicKey> entry : keys.entrySet()) {
			sb.append(entry.getKey().toString()).append('\n').append(Base64.getEncoder().encodeToString(entry.getValue().getEncoded())).append('\n');
		}
		Files.writeString(conf.toPath(), sb);
	}

	static {
		try {
			String s = Files.readString(new File("ocaip.pass").toPath()).trim();
			if (!s.isBlank()) password = s;
		} catch (Exception ignore) {
		}
		try {
			List<String> ln = Files.readAllLines(conf.toPath());
			for (int i = 1; i <= ln.size() / 2; i += 2)
				keys.put(UUID.fromString(ln.get(i - 1)), new EdDSAPublicKey(new X509EncodedKeySpec(Base64.getDecoder().decode(ln.get(i)))));
		} catch (FileSystemException e) {
			try {
				conf.createNewFile();
			} catch (Exception ignore) {
			}
		} catch (Exception ignore) {
		}
	}
}
