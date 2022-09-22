package sf.ssf.sfort.ocaip;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.KeyPairGenerator;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;

import java.io.File;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Base64;

@Environment(EnvType.CLIENT)
public class Tape {
	public static KeyPair key;
	public static final Path path = new File("ocaip.key").toPath();
	public static AuthObject auth = null;
	static {
		Reel.createDir();
		try {
			byte[] seed = Base64.getDecoder().decode(Files.readString(path));
			EdDSAPrivateKeySpec privKey = new EdDSAPrivateKeySpec(seed, Reel.edParams);
			key = new KeyPair(new EdDSAPublicKey(new EdDSAPublicKeySpec(privKey.getA(), Reel.edParams)), new EdDSAPrivateKey(privKey));
		} catch (FileSystemException e) {
			try {
				key = new KeyPairGenerator().generateKeyPair();
				Files.writeString(path, Base64.getEncoder().encodeToString(((EdDSAPrivateKey)key.getPrivate()).getSeed()));
			}catch (Exception ignore){
				Reel.log.error("");
			}
		} catch (Exception ignore) { }
	}
}
