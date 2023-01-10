package sf.ssf.sfort.ocaip;

public class AuthObject {
	public final String pass;
	public final String pow;
	public final String pow512;

	public AuthObject(String pass, String pow, String pow512) {
		this.pass = pass;
		this.pow = pow;
		this.pow512 = pow512;
	}
}
