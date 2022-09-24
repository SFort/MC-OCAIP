package sf.ssf.sfort.ocaip;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.KeyPairGenerator;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import java.io.File;
import java.nio.file.CopyOption;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.Base64;

@Environment(EnvType.CLIENT)
public class Tape {
	public static KeyPair key;
	public static final Path path = new File("ocaip.key").toPath();
	public static final File skinFile = new File("ocaip_skin.png");
	public static byte[] localSkin = null;
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
		try {
			if (skinFile.isFile()) {
				String uuid = PlayerEntity.getOfflinePlayerUuid(MinecraftClient.getInstance().getSession().getUsername()).toString();
				Path uuidPath = new File(Reel.skinDir+"/"+uuid+".png").toPath();
				Files.copy(skinFile.toPath(), uuidPath, StandardCopyOption.REPLACE_EXISTING);
				MessageDigest md = MessageDigest.getInstance("MD5");
				localSkin = Files.readAllBytes(uuidPath);
				Reel.uuidToSkinHash.put(uuid, md.digest());
				OfflineSkinResourcePack.hasTextureCache.put(uuid, true);
			}
		} catch (Exception ignore) {
			Reel.log.error("Could not copy user skin file to skins");
		}
	}
	public static boolean hasOfflineSkin(String s) {
		return OfflineSkinResourcePack.hasIs(s);
	}
}
