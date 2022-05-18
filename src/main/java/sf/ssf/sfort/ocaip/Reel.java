package sf.ssf.sfort.ocaip;


import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Reel {
	public static final Logger log = LogManager.getLogger("OCAIP");
	public static final int protocalVersion = 1;
	public static final EdDSAParameterSpec edParams = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
}
