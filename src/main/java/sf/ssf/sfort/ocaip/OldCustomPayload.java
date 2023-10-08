package sf.ssf.sfort.ocaip;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.login.LoginQueryResponsePayload;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestPayload;
import net.minecraft.util.Identifier;

public abstract class OldCustomPayload {
	public Identifier id = null;
	public PacketByteBuf buf = null;

	public OldCustomPayload(Identifier id, PacketByteBuf buf) {
		this.id = id;
		this.buf = buf;
	}

	public OldCustomPayload(PacketByteBuf buf) {
		this.id = buf.readIdentifier();
		byte[] arr = buf.readByteArray();
		this.buf = new PacketByteBuf(Unpooled.buffer(arr.length));
		this.buf.writeBytes(arr);
	}

	public static class LoginRequest extends OldCustomPayload implements LoginQueryRequestPayload {

		public LoginRequest(Identifier id, PacketByteBuf buf) {
			super(id, buf);
		}

		public LoginRequest(PacketByteBuf buf) {
			super(buf);
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeIdentifier(this.id);
			buf.writeByteArray(this.buf.capacity(this.buf.readableBytes()).array());
		}

		@Override
		public Identifier id() {
			return id;
		}
	}

	public static class LoginResponse extends OldCustomPayload implements LoginQueryResponsePayload {

		public LoginResponse(Identifier id, PacketByteBuf buf) {
			super(id, buf);
		}

		public LoginResponse(PacketByteBuf buf) {
			super(buf);
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeIdentifier(this.id);
			buf.writeByteArray(this.buf.capacity(this.buf.readableBytes()).array());
		}

		public Identifier id() {
			return id;
		}
	}

	public static class Custom extends OldCustomPayload implements CustomPayload {

		public Custom(Identifier id, PacketByteBuf buf) {
			super(id, buf);
		}

		public Custom(PacketByteBuf buf) {
			super(buf);
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeIdentifier(this.id);
			buf.writeByteArray(this.buf.capacity(this.buf.readableBytes()).array());
		}

		@Override
		public Identifier id() {
			return id;
		}
	}

}
