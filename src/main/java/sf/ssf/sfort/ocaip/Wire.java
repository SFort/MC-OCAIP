package sf.ssf.sfort.ocaip;

import net.i2p.crypto.eddsa.EdDSAPublicKey;
import tf.ssf.sfort.ini.SFIni;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wire {
	public static final File conf = new File(Reel.dir+"/server_keys.sf.ini");

	public static Map<String, PublicKey> keys = new HashMap<>();
	public static String password = null;

	public static POW pow = null;
	public static boolean requireOCAIP = false;
	public static boolean disableFAPIjank = true;

	public static void addAndWrite(String name, PublicKey key) throws IOException {
		if (!keys.containsKey(name)) {
			keys.put(name, key);
			BufferedWriter bw = new BufferedWriter(new FileWriter(conf, true));
			bw.write("\n"+name+"="+(key == null ? "" :Base64.getEncoder().encodeToString(key.getEncoded())));
			bw.close();
		}
	}

	static {
		Reel.createDir();
		loadKeys();
		loadConf();
	}
	public static void loadKeys() {
		SFIni sfIni = new SFIni();
		loadLegacyKeys(sfIni);
		int hash = 0;
		try {
			if (conf.isFile()) {
				String text = Files.readString(conf.toPath());
				hash = text.hashCode();
				sfIni.load(text);
			}
		} catch (IOException e) {
			Reel.log.error("OCAIP failed to load keys", e);
		}
		for (Map.Entry<String, List<SFIni.Data>> entry : sfIni.data.entrySet()) {
			List<SFIni.Data> l = entry.getValue();
			if (l == null) continue;
			for (int i=l.size()-1; i>=0; i--) {
				try {
					SFIni.Data d = l.get(i);
					keys.put(entry.getKey(), d.val == null || d.val.isBlank() ? null : new EdDSAPublicKey(new X509EncodedKeySpec(Base64.getDecoder().decode(d.val))));
				} catch (Exception e) {
					l.remove(i);
					Reel.log.error("OCAIP found and removed an invalid key: "+entry.getKey(), e);
				}
			}
		}
		try {
			String iniStr = sfIni.toString();
			if (hash == 0 || hash != iniStr.hashCode()) {
				Files.write(conf.toPath(), iniStr.getBytes());
			}
		} catch (IOException e) {
			Reel.log.error("OCAIP failed to save keys", e);
		}
	}
	public static void loadLegacyKeys(SFIni inIni) {
		File legacyConfFile = new File(Reel.dir+"/server_keys");
		if (!legacyConfFile.exists()) return;
		try {
			List<String> ln = Files.readAllLines(legacyConfFile.toPath());
			for (int i = 1; i < ln.size(); i += 2) {
				String s = ln.get(i);
				if (s == null) break;
				if (s.isBlank()) continue;
				inIni.data.put(ln.get(i - 1), new ArrayList<>(Collections.singleton(new SFIni.Data(s, null))));
			}
			Files.delete(legacyConfFile.toPath());
			Reel.log.info("OCAIP successfully loaded legacy key file");
		} catch(Exception e) {
			Reel.log.warn("OCAIP failed to load legacy key file", e);
		}
	}
	public static void loadConf() {
		File serverConf = new File(Reel.dir+"/server.sf.ini");
		SFIni defIni = new SFIni();
		defIni.load(String.join("\n", new String[]{
				"; Registration password",
				"password=",
				"; Require OCAIP login",
				"requireOCAIP=false",
				"; Registration sha1pow zero count (complexity, higher value is more time)",
				"sha1pow.zero=",
				"; Registration sha1pow count",
				"sha1pow.count=",
				"; FabricAPI has some really janky injections, this will remove them",
				"; when true other mods logins that depend on fapi might break might not",
				"; when false if fapi is installed players with a vanilla game won't be able to connect",
				"disableFAPIlogin=true",
		}));
		loadLegacyConf(defIni);

		if (!serverConf.exists()) {
			try {
				Files.write(serverConf.toPath(), defIni.toString().getBytes());
				Reel.log.info("OCAIP successfully created config file");
				loadConf(defIni);
			} catch (IOException e) {
				Reel.log.error("OCAIP failed to create config file, using defaults", e);
			}
			return;
		}
		try {
			SFIni ini = new SFIni();
			String text = Files.readString(serverConf.toPath());
			int hash = text.hashCode();
			ini.load(text);
			for (Map.Entry<String, List<SFIni.Data>> entry : defIni.data.entrySet()) {
				List<SFIni.Data> list = ini.data.get(entry.getKey());
				if (list == null || list.isEmpty()) {
					ini.data.put(entry.getKey(), entry.getValue());
				} else {
					list.get(0).comments = entry.getValue().get(0).comments;
				}
			}
			loadConf(ini);
			String iniStr = ini.toString();
			if (hash != iniStr.hashCode()) {
				Files.write(serverConf.toPath(), iniStr.getBytes());
			}
		} catch (IOException e) {
			Reel.log.error("OCAIP failed to load config file, using defaults", e);
		}
	}
	public static void loadConf(SFIni ini) {
		String p = ini.getLast("password");
		if (p != null && !p.isBlank())
			password = p;
		pow:try {
			SFIni.Data dat = ini.getLastData("sha1pow.zero");
			if (dat == null || dat.val.isBlank()) break pow;
			int zero = Integer.parseInt(dat.val);
			int count = 1;
			powc:try {
				dat = ini.getLastData("sha1pow.count");
				if (dat == null || dat.val.isBlank()) break powc;
				count = ini.getInt("sha1pow.count");
			} catch (IllegalArgumentException e) {
				SFIni.Data d = ini.getLastData("sha1pow.count");
				if (d != null) d.val = "";
				Reel.log.error("OCAIP failed to read sha1pow.count, reset to default");
			}
			pow = new POW(Math.min(100, zero), Math.max(1, count));
		} catch (IllegalArgumentException e) {
			SFIni.Data d = ini.getLastData("sha1pow.zero");
			if (d != null) d.val = "";
			Reel.log.error("OCAIP failed to read sha1pow.zero, reset to default");
		}
		try {
			requireOCAIP = ini.getBoolean("requireOCAIP");
		} catch (IllegalArgumentException e) {
			SFIni.Data d = ini.getLastData("requireOCAIP");
			if (d != null) d.val = "false";
			Reel.log.error("OCAIP failed to read requireOCAIP, reset to default", e);
		}
		try {
			disableFAPIjank = ini.getBoolean("disableFAPIlogin");
		} catch (IllegalArgumentException e) {
			SFIni.Data d = ini.getLastData("disableFAPIlogin");
			if (d != null) d.val = "true";
			Reel.log.error("OCAIP failed to read disableFAPIlogin, reset to default", e);
		}
	}
	public static void loadLegacyConf(SFIni ini) {
		File legacyConfFile = new File(Reel.dir + "/password");
		if (legacyConfFile.isFile()) {
			try {
				String s = Files.readString(legacyConfFile.toPath()).trim();
				if (!s.isBlank()) {
					SFIni.Data d = ini.getLastData("password");
					if (d != null) {
						d.val = s;
					}
				}
				Files.delete(legacyConfFile.toPath());
			} catch (IOException e) {
				Reel.log.error("OCAIP Failed to load legacy password file", e);
			}
		}
		legacyConfFile = new File(Reel.dir + "/sha1pow");
		if (legacyConfFile.isFile()) {
			try {
				String s = Files.readString(legacyConfFile.toPath()).trim();
				if (!s.isBlank()) {
					int i = s.indexOf('*');
					int count = 1;
					int zeros = 0;
					if (i != -1) {
						count = Integer.parseInt(s.substring(i + 1));
						zeros = Integer.parseInt(s.substring(0, i));
					} else {
						zeros = Integer.parseInt(s);
					}
					if (zeros > 0) {
						SFIni.Data d = ini.getLastData("sha1pow.zero");
						if (d != null) d.val = Integer.toString(Math.min(100, zeros));
						d = ini.getLastData("sha1pow.count");
						if (d != null) d.val = Integer.toString(Math.max(1, count));
					}
				}
				Files.delete(legacyConfFile.toPath());
			} catch (IOException e) {
				Reel.log.error("OCAIP Failed to load legacy sha1pow file", e);
			}
		}
	}
}
