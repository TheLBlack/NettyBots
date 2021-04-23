package mc.thelblack.bots;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import io.netty.buffer.ByteBuf;

public class SimplePacket {

	private static byte[] decompress(byte[] data) throws IOException, DataFormatException {
		Inflater inflater = new Inflater();
		inflater.setInput(data);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		byte[] buffer = new byte[1024];
		while (!inflater.finished()) {
			int count = inflater.inflate(buffer);
			outputStream.write(buffer, 0, count);
		}
		
		outputStream.close();
		return outputStream.toByteArray();
	}
	
	private int id;
	private McByteBuf buf;
	
	private boolean needDecompression;
	private boolean isDecompressed = false;
	
	public SimplePacket(int id, ByteBuf buf, boolean shoudBeDecompressed) {
		this.id = id;
		this.buf = new McByteBuf(buf);
		this.needDecompression = shoudBeDecompressed;
	}
	
	public SimplePacket decompressIfNeeds() throws IOException, DataFormatException {
		if (this.needsDecompression() && !this.isDecompressed()) {
			byte[] bytes = new byte[this.getByteBuf().readableBytes()];
			this.buf.readBytes(bytes);
			
			this.buf.clear();
			this.buf.writeBytes(SimplePacket.decompress(bytes));
			this.id = this.buf.readVarInt();
			
			this.isDecompressed = true;
		}
		
		return this;
	}
	
	public int getPacketId() {
		return this.id;
	}
	
	public McByteBuf getByteBuf() {
		return this.buf;
	}
	
	public boolean isDecompressed() {
		return this.isDecompressed;
	}
	
	public boolean needsDecompression() {
		return this.needDecompression;
	}
}
