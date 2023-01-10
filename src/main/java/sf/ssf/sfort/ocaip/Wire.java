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
	public static final File conf = new File(Reel.dir+"/server_keys");
	public static final File confPass = new File(Reel.dir+"/password");
	public static final File confPow = new File(Reel.dir+"/sha1pow");
	public static final File confPow512 = new File(Reel.dir+"/sha512pow");

	public static Map<String, PublicKey> keys = new HashMap<>();
	public static String password = null;

	public static POW pow = null;
	public static POW512 pow512 = null;

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
			String s = Files.readString(confPass.toPath()).trim();
			if (!s.isBlank()) password = s;
		} catch (Exception ignore) {
		}
		try {
			List<String> ln = Files.readAllLines(conf.toPath());
			for (int i = 1; i < ln.size(); i += 2) {
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
		try {
			String s = Files.readString(confPow.toPath()).trim();
			if (!s.isBlank()) {
				int i = s.indexOf('*');
				int count = 1;
				int zeros = 0;
				if (i != -1) {
					count = Integer.parseInt(s.substring(i+1));
					zeros = Integer.parseInt(s.substring(0, i));
				} else {
					zeros = Integer.parseInt(s);
				}
				if (zeros > 0) {
					Wire.pow = new POW(Math.min(100, zeros), Math.max(1, count));
				}
			}
		} catch (Exception ignore) {
		}
		try {
			String s = Files.readString(confPow512.toPath()).trim();
			if (!s.isBlank()) {
				int i = s.indexOf('*');
				int count = 1;
				int zeros = 0;
				if (i != -1) {
					count = Integer.parseInt(s.substring(i+1));
					zeros = Integer.parseInt(s.substring(0, i));
				} else {
					zeros = Integer.parseInt(s);
				}
				if (zeros > 0) {
					Wire.pow512 = new POW512(Math.min(200, zeros), Math.max(1, count));
				}
			}
		} catch (Exception ignore) {
		}
	}
}
