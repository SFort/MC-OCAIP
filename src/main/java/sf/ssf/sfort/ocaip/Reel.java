package sf.ssf.sfort.ocaip;


import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;

public class Reel {
	public static final Logger log = LogManager.getLogger("OCAIP");
	public static final int protocalVersion = 2;
	public static final EdDSAParameterSpec edParams = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);

	public static void createDir(){
		createDir("ocaip");
	}
	public static void createDir(String str){
		try {
			Files.createDirectories(new File(str).toPath());
		} catch (Exception ignore) {}
	}
}
