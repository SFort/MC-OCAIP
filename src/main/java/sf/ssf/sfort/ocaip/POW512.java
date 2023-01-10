package sf.ssf.sfort.ocaip;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class POW512 {

	public static String computeSolution(String recvPow, AtomicBoolean canceled) {
		int count = Integer.valueOf(recvPow.substring(0, recvPow.indexOf(',')), 36);
		byte[] num = new byte[4];
		new Random().nextBytes(num);
		Base64.Encoder encoder = Base64.getEncoder();
		while (leadingZeros(recvPow + "," + encoder.encodeToString(num)) < count) {
			if (canceled.get()) return null;
			num = POW.increment(num);
		}
		return recvPow+","+encoder.encodeToString(num);
	}
	public static int leadingZeros(String in){
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(in.getBytes(StandardCharsets.UTF_8));
			return POW.leadingZeros(md.digest());
		} catch (NoSuchAlgorithmException e) {
			Reel.log.error("Failed to get sha512", e);
		}
		return 0;
	}
	public static Future<String> computeSolutionAsync(String recvPow, Runnable onDone, Runnable onFail, AtomicBoolean canceled) {
		LinkedList<CompletableFuture<String>> futures = new LinkedList<>();
		for (String s : recvPow.split(";")) {
			futures.add(CompletableFuture.supplyAsync(()->computeSolution(s, canceled)));
		}
		return CompletableFuture.supplyAsync(() -> {
			StringBuilder sb = new StringBuilder();
			try {
				CompletableFuture<String> f = futures.poll();
				if (f!=null) sb.append(f.get());
				for (CompletableFuture<String> future : futures) {
					if (canceled.get()) return null;
					sb.append(';');
					sb.append(future.get());
				}
			} catch (InterruptedException | ExecutionException e) {
				Reel.log.error("Error computing pow", e);
				onFail.run();
				return null;
			}
			if (canceled.get()) return null;
			onDone.run();
			return sb.toString();
		});
	}
	public final int zeroLead;
	public final int count;
	public final Map<String, String[]> sessionQueries = new HashMap<>();
	public POW512(int zeroLead, int count) {
		this.zeroLead = zeroLead;
		this.count = count;
	}
	public String genPrompt(String name, Random random){
		{
			String[] ret = sessionQueries.get(name);
			if (ret!=null && ret.length>0) return String.join(";", ret);
		}
		byte[] bytes = new byte[256];
		String zeros = Integer.toString(zeroLead, 36);
		String[] query = new String[count];
		for (int i=0;i<count;i++){
			random.nextBytes(bytes);
			query[i] = zeros+','+Base64.getEncoder().encodeToString(bytes);
		}
		sessionQueries.put(name, query);
		return String.join(";", query);
	}
	public boolean isResponseValid(String name, String recvPow){
		if (recvPow == null) return false;
		String[] pows = sessionQueries.get(name);
		if (pows == null) return false;
		String[] recvPows = recvPow.split(";");
		if (recvPows.length != pows.length) return false;
		for (int i=0; i<pows.length; i++){
			if (!recvPows[i].startsWith(pows[i])) return false;
			if (leadingZeros(recvPows[i])<this.count){
				sessionQueries.remove(name);
				return false;
			}
		}
		sessionQueries.remove(name);
		return true;
	}
}
