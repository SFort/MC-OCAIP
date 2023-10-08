package sf.ssf.sfort.ocaip;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.login.LoginQueryResponsePayload;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestPayload;
import net.minecraft.util.Identifier;

public class OldCustomPayload implements CustomPayload, LoginQueryResponsePayload, LoginQueryRequestPayload {
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
