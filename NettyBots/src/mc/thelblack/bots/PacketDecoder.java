package mc.thelblack.bots;

import java.io.IOException;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;

public class PacketDecoder extends ByteToMessageDecoder {

	private static int readVarInt(ByteBuf in) throws IOException {
		int i = 0;
		int j = 0;

		while (true) {
			int k = in.readByte();
			i |= (k & 0x7F) << j++ * 7;
			if (j > 5) throw new RuntimeException("VarInt too big");
			if ((k & 0x80) != 128) break;
		}

		return i;
	}

	private Integer dynamicBytes = null, compression = -1;
	private boolean playState = false;

	public PacketDecoder() {}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() > 2097151) throw new TooLongFrameException();
		else if (in.readableBytes() >= 5) {
			if (this.dynamicBytes == null) this.dynamicBytes = PacketDecoder.readVarInt(in);
			if (in.readableBytes() >= this.dynamicBytes) {
				SimplePacket packet;
				ByteBuf nb = in.readBytes(this.dynamicBytes);

				if (this.compression > 0) {
					int size = PacketDecoder.readVarInt(nb);

					if (size > 0) packet = new SimplePacket(-1, nb, true);
					else packet = new SimplePacket(PacketDecoder.readVarInt(nb), nb, false);
				} else {
					packet = new SimplePacket(PacketDecoder.readVarInt(nb), nb, false);

					if (!this.playState && packet.getPacketId() == 0x03) {
						this.compression = packet.getByteBuf().readVarInt();
						ctx.channel().attr(Bot.COMPRESSION).set(this.compression);
					}
				}

				if (packet.getPacketId() > 0x04) {
					ctx.channel().attr(Bot.STATE).set(3);
					this.playState = true;
				}

				this.dynamicBytes = null;
				out.add(packet);
			}
		}
	}
}
