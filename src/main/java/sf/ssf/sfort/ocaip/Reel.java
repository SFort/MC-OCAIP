package sf.ssf.sfort.ocaip;


import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import nilloader.NilLoader;
import nilloader.api.ClassTransformer;
import nilloader.api.ModRemapper;
import nilloader.api.NilLogger;
import nilloader.api.lib.asm.ClassReader;
import nilloader.api.lib.asm.ClassWriter;
import nilloader.api.lib.asm.Opcodes;
import nilloader.api.lib.asm.Type;
import nilloader.api.lib.asm.tree.ClassNode;
import nilloader.api.lib.asm.tree.FieldNode;
import nilloader.impl.lib.bombe.type.FieldType;
import nilloader.impl.lib.bombe.type.MethodDescriptor;
import nilloader.impl.lib.bombe.type.signature.FieldSignature;
import nilloader.impl.lib.bombe.type.signature.MethodSignature;
import nilloader.impl.lib.lorenz.MappingSet;
import nilloader.impl.lib.lorenz.model.ClassMapping;
import nilloader.impl.lib.lorenz.model.FieldMapping;
import nilloader.impl.lib.lorenz.model.MethodMapping;
import sf.ssf.sfort.ocaip.nil.NetClientLogin;
import sf.ssf.sfort.ocaip.nil.NetServerLogin;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class Reel implements Runnable {
	public static final NilLogger log = NilLogger.get("OCAIP");
	private static Optional<MappingSet> mappings = Optional.empty();
	public static final int protocalVersion = 1;
	public static final EdDSAParameterSpec edParams = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
	public static void createDir(){
		createDir("ocaip");
	}
	public static void createDir(String str){
		try {
			Files.createDirectories(new File(str).toPath());
		} catch (Exception ignore) {}
	}

	@Override
	public void run() {
		mappings = Optional.ofNullable(NilLoader.getActiveMappings(NilLoader.getActiveMod()));
		//TODO
		ModRemapper.setTargetMapping("default");
		ClassTransformer.register((className, originalData) -> {
			className = mapType(className.replace('.', '/'));
			final Transformer transformer = new Transformer();
			switch (className) {
				case "net/minecraft/client/gui/screen/ConnectScreen":
					transformer.addN(c->c.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "ocaip$pass", Type.getDescriptor(String.class), null, null)));
					break;
				case "net/minecraft/client/network/ClientLoginNetworkHandler":
					transformer.addN(c->c.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "ocaip$recivedRequest", Type.getDescriptor(boolean.class), null, 0)));
					break;
				case "net/minecraft/server/network/ServerLoginNetworkHandler":
					transformer.addN(c->{
						c.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "ocaip$hasBypassed", Type.getDescriptor(byte.class), null, 0b01));
						c.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "ocaip$sentBytes", Type.getDescriptor(byte[].class), null, 0));
					});
					break;
				default:
					return originalData;
			}
			return transformer.run(originalData);
		});
	}
	private static class Transformer{
		private List<Object> list = new ArrayList<>();
		public void addB(ByteTransformer t) {
			list.add(t);
		}
		public void addN(NodeTransformer t) {
			list.add(t);
		}
		public byte[] run(byte[] bytes) {
			for (Object o: list) {
				if (o instanceof ByteTransformer) {
					bytes = ((ByteTransformer)o).process(bytes);
				} else if (o instanceof NodeTransformer) {
					ClassNode clazz = new ClassNode();
					new ClassReader(bytes).accept(clazz, 0);
					((NodeTransformer)o).process(clazz);
					ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
					clazz.accept(writer);
					bytes = writer.toByteArray();
				}
			}
			return bytes;
		}
		@FunctionalInterface
		public interface ByteTransformer {
			byte[] process(byte[] originalData);
		}
		@FunctionalInterface
		public interface NodeTransformer {
			void process(ClassNode clazz);
		}
	}
	public static String obfType(String type) {
		return mappings.flatMap(m -> m.computeClassMapping(type))
				.map(ClassMapping::getFullDeobfuscatedName)
				.orElse(type);
	}
	public static String mapType(String type) {
		return mappings.flatMap(m -> m.computeClassMapping(type))
				.map(ClassMapping::getFullObfuscatedName)
				.orElse(type);
	}
	public static String obfField(String owner, String name, String desc) {
		return mappings.flatMap(m -> m.computeClassMapping(owner))
				.flatMap(cm -> cm.computeFieldMapping(FieldSignature.of(name, desc)))
				.map(FieldMapping::getDeobfuscatedName)
				.orElse(name);
	}
	public static String obfMethod(String owner, String name, String desc) {
		return mappings.flatMap(m -> m.computeClassMapping(owner))
				.flatMap(cm -> cm.getMethodMapping(name, desc))
				.map(MethodMapping::getDeobfuscatedSignature)
				.map(MethodSignature::getName)
				.orElse(name);
	}
	public static String obfMethodDesc(String desc) {
		return mappings.map(m -> m.deobfuscate(MethodDescriptor.of(desc)))
				.map(MethodDescriptor::toString)
				.orElse(desc);
	}
	public static String obfFieldDesc(String desc) {
		return mappings.map(m -> m.deobfuscate(FieldType.of(desc)))
				.map(FieldType::toString)
				.orElse(desc);
	}

}
