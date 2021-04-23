package mc.thelblack.bots;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ByteProcessor;

public class McByteBuf extends ByteBuf {

	private ByteBuf a;

	public McByteBuf(ByteBuf buf) {
		this.a = buf;
	}
	
	public McByteBuf writeVarInt(int value) {
        while (true) {
            if ((value & 0xFFFFFF80) == 0) {
              this.writeByte(value);
              return this;
            }

            this.writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }
	}
	
	public int readVarInt() {
        int i = 0;
        int j = 0;
        
        while (true) {
            int k = this.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) throw new RuntimeException("VarInt too big");
            if ((k & 0x80) != 128) break;
        }
        
        return i;
	}
	
	public McByteBuf writeBytes(String string) {
		this.a.writeBytes(string.getBytes());
		
		return this;
	}
	
	public McByteBuf writeString(String string) {
		this.writeVarInt(string.length());
		this.writeBytes(string);
		
		return this;
	}
	
	public String readString() {
		int size = this.readVarInt();
		byte[] bytes = new byte[size];
		
		this.a.readBytes(bytes);
		return new String(bytes);
	}

	public int capacity() {
		return this.a.capacity();
	}

	public McByteBuf capacity(int i) {
		this.a.capacity(i);
		
		return this;
	}

	public int maxCapacity() {
		return this.a.maxCapacity();
	}

	public ByteBufAllocator alloc() {
		return this.a.alloc();
	}

	@SuppressWarnings("deprecation")
	public ByteOrder order() {
		return this.a.order();
	}

	@SuppressWarnings("deprecation")
	public McByteBuf order(ByteOrder byteorder) {
		this.a.order(byteorder);
	
		return this;
	}

	public McByteBuf unwrap() {
		this.a.unwrap();
		
		return this;
	}

	public boolean isDirect() {
		return this.a.isDirect();
	}

	public boolean isReadOnly() {
		return this.a.isReadOnly();
	}

	public McByteBuf asReadOnly() {
		this.a.asReadOnly();
		
		return this;
	}

	public int readerIndex() {
		return this.a.readerIndex();
	}

	public McByteBuf readerIndex(int i) {
		this.a.readerIndex(i);
		
		return this;
	}

	public int writerIndex() {
		return this.a.writerIndex();
	}

	public McByteBuf writerIndex(int i) {
		this.a.writerIndex(i);
		
		return this;
	}

	public McByteBuf setIndex(int i, int j) {
		this.a.setIndex(i, j);
		
		return this;
	}

	public int readableBytes() {
		return this.a.readableBytes();
	}

	public int writableBytes() {
		return this.a.writableBytes();
	}

	public int maxWritableBytes() {
		return this.a.maxWritableBytes();
	}

	public boolean isReadable() {
		return this.a.isReadable();
	}

	public boolean isReadable(int i) {
		return this.a.isReadable(i);
	}

	public boolean isWritable() {
		return this.a.isWritable();
	}

	public boolean isWritable(int i) {
		return this.a.isWritable(i);
	}

	public McByteBuf clear() {
		this.a.clear();
		
		return this;
	}

	public McByteBuf markReaderIndex() {
		this.a.markReaderIndex();
		
		return this;
	}

	public McByteBuf resetReaderIndex() {
		this.a.resetReaderIndex();
		
		return this;
	}

	public McByteBuf markWriterIndex() {
		this.a.markWriterIndex();
		
		return this;
	}

	public McByteBuf resetWriterIndex() {
		this.a.resetWriterIndex();
		
		return this;
	}

	public McByteBuf discardReadBytes() {
		this.a.discardReadBytes();
		
		return this;
	}

	public McByteBuf discardSomeReadBytes() {
		this.a.discardSomeReadBytes();
		
		return this;
	}

	public McByteBuf ensureWritable(int i) {
		this.a.ensureWritable(i);
		
		return this;
	}

	public int ensureWritable(int i, boolean flag) {
		return this.a.ensureWritable(i, flag);
	}

	public boolean getBoolean(int i) {
		return this.a.getBoolean(i);
	}

	public byte getByte(int i) {
		return this.a.getByte(i);
	}

	public short getUnsignedByte(int i) {
		return this.a.getUnsignedByte(i);
	}

	public short getShort(int i) {
		return this.a.getShort(i);
	}

	public short getShortLE(int i) {
		return this.a.getShortLE(i);
	}

	public int getUnsignedShort(int i) {
		return this.a.getUnsignedShort(i);
	}

	public int getUnsignedShortLE(int i) {
		return this.a.getUnsignedShortLE(i);
	}

	public int getMedium(int i) {
		return this.a.getMedium(i);
	}

	public int getMediumLE(int i) {
		return this.a.getMediumLE(i);
	}

	public int getUnsignedMedium(int i) {
		return this.a.getUnsignedMedium(i);
	}

	public int getUnsignedMediumLE(int i) {
		return this.a.getUnsignedMediumLE(i);
	}

	public int getInt(int i) {
		return this.a.getInt(i);
	}

	public int getIntLE(int i) {
		return this.a.getIntLE(i);
	}

	public long getUnsignedInt(int i) {
		return this.a.getUnsignedInt(i);
	}

	public long getUnsignedIntLE(int i) {
		return this.a.getUnsignedIntLE(i);
	}

	public long getLong(int i) {
		return this.a.getLong(i);
	}

	public long getLongLE(int i) {
		return this.a.getLongLE(i);
	}

	public char getChar(int i) {
		return this.a.getChar(i);
	}

	public float getFloat(int i) {
		return this.a.getFloat(i);
	}

	public double getDouble(int i) {
		return this.a.getDouble(i);
	}

	public McByteBuf getBytes(int i, ByteBuf bytebuf) {
		this.a.getBytes(i, bytebuf);
		
		return this;
	}

	public McByteBuf getBytes(int i, ByteBuf bytebuf, int j) {
		this.a.getBytes(i, bytebuf, j);
		
		return this;
	}

	public McByteBuf getBytes(int i, ByteBuf bytebuf, int j, int k) {
		this.a.getBytes(i, bytebuf, j, k);
		
		return this;
	}

	public McByteBuf getBytes(int i, byte[] abyte) {
		this.a.getBytes(i, abyte);
		
		return this;
	}

	public McByteBuf getBytes(int i, byte[] abyte, int j, int k) {
		this.a.getBytes(i, abyte, j, k);
		
		return this;
	}

	public McByteBuf getBytes(int i, ByteBuffer bytebuffer) {
		this.a.getBytes(i, bytebuffer);
		
		return this;
	}

	public McByteBuf getBytes(int i, OutputStream outputstream, int j) throws IOException {
		this.a.getBytes(i, outputstream, j);
		
		return this;
	}

	public int getBytes(int i, GatheringByteChannel gatheringbytechannel, int j) throws IOException {
		return this.a.getBytes(i, gatheringbytechannel, j);
	}

	public int getBytes(int i, FileChannel filechannel, long j, int k) throws IOException {
		return this.a.getBytes(i, filechannel, j, k);
	}

	public CharSequence getCharSequence(int i, int j, Charset charset) {
		return this.a.getCharSequence(i, j, charset);
	}

	public McByteBuf setBoolean(int i, boolean flag) {
		this.a.setBoolean(i, flag);
		
		return this;
	}

	public McByteBuf setByte(int i, int j) {
		this.a.setByte(i, j);
		
		return this;
	}

	public McByteBuf setShort(int i, int j) {
		this.a.setShort(i, j);
		
		return this;
	}

	public McByteBuf setShortLE(int i, int j) {
		this.a.setShortLE(i, j);
		
		return this;
	}

	public McByteBuf setMedium(int i, int j) {
		this.a.setMedium(i, j);
		
		return this;
	}

	public McByteBuf setMediumLE(int i, int j) {
		this.a.setMediumLE(i, j);
		
		return this;
	}

	public McByteBuf setInt(int i, int j) {
		this.a.setInt(i, j);
		
		return this;
	}

	public McByteBuf setIntLE(int i, int j) {
		this.a.setIntLE(i, j);
		
		return this;
	}

	public McByteBuf setLong(int i, long j) {
		this.a.setLong(i, j);
		
		return this;
	}

	public McByteBuf setLongLE(int i, long j) {
		this.a.setLongLE(i, j);
		
		return this;
	}

	public McByteBuf setChar(int i, int j) {
		this.a.setChar(i, j);
		
		return this;
	}

	public McByteBuf setFloat(int i, float f) {
		this.a.setFloat(i, f);
		
		return this;
	}

	public McByteBuf setDouble(int i, double d0) {
		this.a.setDouble(i, d0);
		
		return this;
	}

	public McByteBuf setBytes(int i, ByteBuf bytebuf) {
		this.a.setBytes(i, bytebuf);
		
		return this;
	}

	public McByteBuf setBytes(int i, ByteBuf bytebuf, int j) {
		this.a.setBytes(i, bytebuf, j);
		
		return this;
	}

	public McByteBuf setBytes(int i, ByteBuf bytebuf, int j, int k) {
		this.a.setBytes(i, bytebuf, j, k);
		
		return this;
	}

	public McByteBuf setBytes(int i, byte[] abyte) {
		this.a.setBytes(i, abyte);
		
		return this;
	}

	public McByteBuf setBytes(int i, byte[] abyte, int j, int k) {
		this.a.setBytes(i, abyte, j, k);
		
		return this;
	}

	public McByteBuf setBytes(int i, ByteBuffer bytebuffer) {
		this.a.setBytes(i, bytebuffer);
		
		return this;
	}

	public int setBytes(int i, InputStream inputstream, int j) throws IOException {
		return this.a.setBytes(i, inputstream, j);
	}

	public int setBytes(int i, ScatteringByteChannel scatteringbytechannel, int j) throws IOException {
		return this.a.setBytes(i, scatteringbytechannel, j);
	}

	public int setBytes(int i, FileChannel filechannel, long j, int k) throws IOException {
		return this.a.setBytes(i, filechannel, j, k);
	}

	public McByteBuf setZero(int i, int j) {
		this.a.setZero(i, j);
		
		return this;
	}

	public int setCharSequence(int i, CharSequence charsequence, Charset charset) {
		return this.a.setCharSequence(i, charsequence, charset);
	}

	public boolean readBoolean() {
		return this.a.readBoolean();
	}

	public byte readByte() {
		return this.a.readByte();
	}

	public short readUnsignedByte() {
		return this.a.readUnsignedByte();
	}

	public short readShort() {
		return this.a.readShort();
	}

	public short readShortLE() {
		return this.a.readShortLE();
	}

	public int readUnsignedShort() {
		return this.a.readUnsignedShort();
	}

	public int readUnsignedShortLE() {
		return this.a.readUnsignedShortLE();
	}

	public int readMedium() {
		return this.a.readMedium();
	}

	public int readMediumLE() {
		return this.a.readMediumLE();
	}

	public int readUnsignedMedium() {
		return this.a.readUnsignedMedium();
	}

	public int readUnsignedMediumLE() {
		return this.a.readUnsignedMediumLE();
	}

	public int readInt() {
		return this.a.readInt();
	}

	public int readIntLE() {
		return this.a.readIntLE();
	}

	public long readUnsignedInt() {
		return this.a.readUnsignedInt();
	}

	public long readUnsignedIntLE() {
		return this.a.readUnsignedIntLE();
	}

	public long readLong() {
		return this.a.readLong();
	}

	public long readLongLE() {
		return this.a.readLongLE();
	}

	public char readChar() {
		return this.a.readChar();
	}

	public float readFloat() {
		return this.a.readFloat();
	}

	public double readDouble() {
		return this.a.readDouble();
	}

	public McByteBuf readBytes(int i) {
		this.a.readBytes(i);
		
		return this;
	}

	public McByteBuf readSlice(int i) {
		this.a.readSlice(i);
		
		return this;
	}

	public McByteBuf readRetainedSlice(int i) {
		this.a.readRetainedSlice(i);
		
		return this;
	}

	public McByteBuf readBytes(ByteBuf bytebuf) {
		this.a.readBytes(bytebuf);
		
		return this;
	}

	public McByteBuf readBytes(ByteBuf bytebuf, int i) {
		this.a.readBytes(bytebuf, i);
		
		return this;
	}

	public McByteBuf readBytes(ByteBuf bytebuf, int i, int j) {
		this.a.readBytes(bytebuf, i, j);
		
		return this;
	}

	public McByteBuf readBytes(byte[] abyte) {
		this.a.readBytes(abyte);
		
		return this;
	}

	public McByteBuf readBytes(byte[] abyte, int i, int j) {
		this.a.readBytes(abyte, i, j);
		
		return this;
	}

	public McByteBuf readBytes(ByteBuffer bytebuffer) {
		this.a.readBytes(bytebuffer);
		
		return this;
	}

	public McByteBuf readBytes(OutputStream outputstream, int i) throws IOException {
		this.a.readBytes(outputstream, i);
		
		return this;
	}

	public int readBytes(GatheringByteChannel gatheringbytechannel, int i) throws IOException {
		return this.a.readBytes(gatheringbytechannel, i);
	}

	public CharSequence readCharSequence(int i, Charset charset) {
		return this.a.readCharSequence(i, charset);
	}

	public int readBytes(FileChannel filechannel, long i, int j) throws IOException {
		return this.a.readBytes(filechannel, i, j);
	}

	public McByteBuf skipBytes(int i) {
		this.a.skipBytes(i);
		
		return this;
	}

	public McByteBuf writeBoolean(boolean flag) {
		this.a.writeBoolean(flag);
		
		return this;
	}

	public McByteBuf writeByte(int i) {
		this.a.writeByte(i);
		
		return this;
	}

	public McByteBuf writeShort(int i) {
		this.a.writeShort(i);
		
		return this;
	}

	public McByteBuf writeShortLE(int i) {
		this.a.writeShortLE(i);
		
		return this;
	}

	public McByteBuf writeMedium(int i) {
		this.a.writeMedium(i);
		
		return this;
	}

	public McByteBuf writeMediumLE(int i) {
		this.a.writeMediumLE(i);
		
		return this;
	}

	public McByteBuf writeInt(int i) {
		this.a.writeInt(i);
		
		return this;
	}

	public McByteBuf writeIntLE(int i) {
		this.a.writeIntLE(i);
		
		return this;
	}

	public McByteBuf writeLong(long i) {
		this.a.writeLong(i);
		
		return this;
	}

	public McByteBuf writeLongLE(long i) {
		this.a.writeLongLE(i);
		
		return this;
	}

	public McByteBuf writeChar(int i) {
		this.a.writeChar(i);
		
		return this;
	}

	public McByteBuf writeFloat(float f) {
		this.a.writeFloat(f);
		
		return this;
	}

	public McByteBuf writeDouble(double d0) {
		this.a.writeDouble(d0);
		
		return this;
	}

	public McByteBuf writeBytes(ByteBuf bytebuf) {
		this.a.writeBytes(bytebuf);
		
		return this;
	}

	public McByteBuf writeBytes(ByteBuf bytebuf, int i) {
		this.a.writeBytes(bytebuf, i);
		
		return this;
	}

	public McByteBuf writeBytes(ByteBuf bytebuf, int i, int j) {
		this.a.writeBytes(bytebuf, i, j);
		
		return this;
	}

	public McByteBuf writeBytes(byte[] abyte) {
		this.a.writeBytes(abyte);
		
		return this;
	}

	public McByteBuf writeBytes(byte[] abyte, int i, int j) {
		this.a.writeBytes(abyte, i, j);
		
		return this;
	}

	public McByteBuf writeBytes(ByteBuffer bytebuffer) {
		this.a.writeBytes(bytebuffer);
		
		return this;
	}

	public int writeBytes(InputStream inputstream, int i) throws IOException {
		return this.a.writeBytes(inputstream, i);
	}

	public int writeBytes(ScatteringByteChannel scatteringbytechannel, int i) throws IOException {
		return this.a.writeBytes(scatteringbytechannel, i);
	}

	public int writeBytes(FileChannel filechannel, long i, int j) throws IOException {
		return this.a.writeBytes(filechannel, i, j);
	}

	public McByteBuf writeZero(int i) {
		this.a.writeZero(i);
		
		return this;
	}

	public int writeCharSequence(CharSequence charsequence, Charset charset) {
		return this.a.writeCharSequence(charsequence, charset);
	}

	public int indexOf(int i, int j, byte b0) {
		return this.a.indexOf(i, j, b0);
	}

	public int bytesBefore(byte b0) {
		return this.a.bytesBefore(b0);
	}

	public int bytesBefore(int i, byte b0) {
		return this.a.bytesBefore(i, b0);
	}

	public int bytesBefore(int i, int j, byte b0) {
		return this.a.bytesBefore(i, j, b0);
	}

	public int forEachByte(ByteProcessor byteprocessor) {
		return this.a.forEachByte(byteprocessor);
	}

	public int forEachByte(int i, int j, ByteProcessor byteprocessor) {
		return this.a.forEachByte(i, j, byteprocessor);
	}

	public int forEachByteDesc(ByteProcessor byteprocessor) {
		return this.a.forEachByteDesc(byteprocessor);
	}

	public int forEachByteDesc(int i, int j, ByteProcessor byteprocessor) {
		return this.a.forEachByteDesc(i, j, byteprocessor);
	}

	public McByteBuf copy() {
		this.a.copy();
		
		return this;
	}

	public McByteBuf copy(int i, int j) {
		this.a.copy(i, j);
		
		return this;
	}

	public McByteBuf slice() {
		this.a.slice();
		
		return this;
	}

	public McByteBuf retainedSlice() {
		this.a.retainedSlice();
		
		return this;
	}

	public McByteBuf slice(int i, int j) {
		this.a.slice(i, j);
		
		return this;
	}

	public McByteBuf retainedSlice(int i, int j) {
		this.a.retainedSlice(i, j);
		
		return this;
	}

	public McByteBuf duplicate() {
		this.a.duplicate();
		
		return this;
	}

	public McByteBuf retainedDuplicate() {
		this.a.retainedDuplicate();
		
		return this;
	}

	public int nioBufferCount() {
		return this.a.nioBufferCount();
	}

	public ByteBuffer nioBuffer() {
		return this.a.nioBuffer();
	}

	public ByteBuffer nioBuffer(int i, int j) {
		return this.a.nioBuffer(i, j);
	}

	public ByteBuffer internalNioBuffer(int i, int j) {
		return this.a.internalNioBuffer(i, j);
	}

	public ByteBuffer[] nioBuffers() {
		return this.a.nioBuffers();
	}

	public ByteBuffer[] nioBuffers(int i, int j) {
		return this.a.nioBuffers(i, j);
	}

	public boolean hasArray() {
		return this.a.hasArray();
	}

	public byte[] array() {
		return this.a.array();
	}

	public int arrayOffset() {
		return this.a.arrayOffset();
	}

	public boolean hasMemoryAddress() {
		return this.a.hasMemoryAddress();
	}

	public long memoryAddress() {
		return this.a.memoryAddress();
	}

	public String toString(Charset charset) {
		return this.a.toString(charset);
	}

	public String toString(int i, int j, Charset charset) {
		return this.a.toString(i, j, charset);
	}

	public int hashCode() {
		return this.a.hashCode();
	}

	public boolean equals(Object object) {
		return this.a.equals(object);
	}

	public int compareTo(ByteBuf bytebuf) {
		return this.a.compareTo(bytebuf);
	}

	public String toString() {
		return this.a.toString();
	}

	public McByteBuf retain(int i) {
		this.a.retain(i);
		
		return this;
	}

	public McByteBuf retain() {
		this.a.retain();
		
		return this;
	}

	public McByteBuf touch() {
		this.a.touch();
		
		return this;
	}

	public McByteBuf touch(Object object) {
		this.a.touch(object);
		
		return this;
	}

	public int refCnt() {
		return this.a.refCnt();
	}

	public boolean release() {
		return this.a.release();
	}

	public boolean release(int i) {
		return this.a.release(i);
	}

}
