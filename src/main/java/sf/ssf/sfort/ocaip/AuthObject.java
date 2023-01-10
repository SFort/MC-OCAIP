package sf.ssf.sfort.ocaip;

public class AuthObject {
	public final String pass;
	public final String pow;
	public final String pow512;
	public final String powArgon2;

	public AuthObject(String pass, String pow, String pow512, String powArgon2) {
		this.pass = pass;
		this.pow = pow;
		this.pow512 = pow512;
		this.powArgon2 = powArgon2;
	}
}
