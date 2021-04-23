package mc.thelblack.bots;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder extends MessageToByteEncoder<McByteBuf> {

	private static void writeVarInt(ByteBuf buf, int value) {
		while (true) {
			if ((value & 0xFFFFFF80) == 0) {
				buf.writeByte(value);
				return;
			}

			buf.writeByte(value & 0x7F | 0x80);
			value >>>= 7;
		}
	}

	private static byte[] compress(byte[] data) throws IOException {
		Deflater deflater = new Deflater();
		deflater.setInput(data);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		deflater.finish();
		byte[] buffer = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer);
			outputStream.write(buffer, 0, count);
		}

		outputStream.close();
		return outputStream.toByteArray();
	}

	public PacketEncoder() {}

	@Override
	protected void encode(ChannelHandlerContext ctx, McByteBuf msg, ByteBuf out) throws Exception {
		int c = ctx.channel().attr(Bot.COMPRESSION).get();

		if (c > 0) {
			if (msg.readableBytes() > c) {
				int deco = msg.readableBytes();
				byte[] bytes = new byte[deco];
				msg.readBytes(bytes);
				byte[] compressed = PacketEncoder.compress(bytes);
				
				ByteBuf buf = ctx.alloc().buffer();
				PacketEncoder.writeVarInt(buf, deco);
				buf.writeBytes(compressed);
				
				PacketEncoder.writeVarInt(msg, buf.readableBytes());
				msg.writeBytes(buf);
				
				buf.release();
				
			} else {
				PacketEncoder.writeVarInt(out, msg.readableBytes()+1);
				PacketEncoder.writeVarInt(out, 0);
				out.writeBytes(msg);
			}
		} else {
			PacketEncoder.writeVarInt(out, msg.readableBytes());
			out.writeBytes(msg);
		}
	}
}
