package sf.ssf.sfort.ocaip;


import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Reel {
	public static final Logger log = LogManager.getLogger("OCAIP");
	public static final int protocalVersion = 2;
	public static final EdDSAParameterSpec edParams = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
	public static final String dir = "ocaip";
	public static final String skinDir = dir+"/skins";
	public static final Map<String, byte[]> uuidToSkinHash = new HashMap<>();

	public static void createDir(){
		createDir(dir);
		createDir(skinDir);
	}
	public static void createDir(String str){
		try {
			Files.createDirectories(new File(str).toPath());
		} catch (Exception ignore) {}
	}
	public static void writeSkin(String name, byte[] data) throws NoSuchAlgorithmException, IOException {
		OfflineSkinResourcePack.hasTextureCache.remove(name);
		name = UUID.fromString(name).toString();
		MessageDigest md = MessageDigest.getInstance("MD5");
		Files.write(new File(skinDir+"/"+name+".png").toPath(), data);
		uuidToSkinHash.put(name, md.digest(data));
	}
	static {
		try {
			File[] skinDir = new File(Reel.skinDir).listFiles();
			MessageDigest md = MessageDigest.getInstance("MD5");
			if (skinDir != null) {
				for (File file : skinDir){
					String name = file.getName();
					if (name.endsWith(".png")) {
						uuidToSkinHash.put(name.substring(0, name.length()-4), md.digest(Files.readAllBytes(file.toPath())));
					}
				}
			}
		} catch (Exception ignore) {}

	}

}
