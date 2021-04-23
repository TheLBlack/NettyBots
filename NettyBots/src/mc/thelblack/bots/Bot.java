package mc.thelblack.bots;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

public class Bot {
	
	private static final EventLoopGroup MAIN_LOOP = new NioEventLoopGroup();
	
	protected static final AttributeKey<Integer> COMPRESSION = AttributeKey.newInstance("compression");
	protected static final AttributeKey<Integer> STATE = AttributeKey.newInstance("state");
	
	private Channel channel;
	
	private InetSocketAddress host;
	private String name;
	
	private Consumer<SimplePacket> listener;
	private Consumer<Throwable> exception;
	private Consumer<Bot> disconnected;
	
	private BotPilot pilot = null;
	
	public Bot(InetSocketAddress adress, String name) {
		this.unregisterListener();
		this.unregisterException();
		this.unregisterDisconnection();
		this.host = adress;
		this.name = name;
	}
	
	public Bot connect() {
		Bootstrap b = new Bootstrap();
		b.group(Bot.MAIN_LOOP).channel(NioSocketChannel.class).remoteAddress(this.host).handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addFirst(new PacketEncoder());
				ch.pipeline().addFirst(new PacketDecoder());
				
				ch.pipeline().addLast(new SimpleChannelInboundHandler<SimplePacket>() {
					@Override
					protected void channelRead0(ChannelHandlerContext ctx, SimplePacket msg) throws Exception {
						if (msg.getPacketId() == 0x1F) {
							Bot.this.sendOnePacket(a -> {
								a.writeVarInt(0x10);
								a.writeLong(msg.getByteBuf().readLong());
							});
						}
						
						Bot.this.listener.accept(msg);
						msg.getByteBuf().release();
					}
					
					@Override
					public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
						Bot.this.exception.accept(cause);
						Bot.this.disconnect();
					}
				});
	
				Bot.this.channel = ch.pipeline().channel();
				Bot.this.channel.closeFuture().removeListener(new ChannelFutureListener() {
				    @Override
				    public void operationComplete(ChannelFuture future) throws Exception {
				        Bot.this.disconnected.accept(Bot.this);
				    }
				});
			}
		}).attr(Bot.COMPRESSION, -1).attr(Bot.STATE, 1).connect().addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture channel) {
				if (channel.isSuccess()) Bot.this.startLogin();
				else {
					Bot.this.exception.accept(channel.cause());
					Bot.this.disconnected.accept(Bot.this);
				}
			}
		});
		
		return this;
	}
	
	private void startLogin() {
		this.channel.attr(Bot.STATE).set(2);
		
		this.sendOnePacket(a -> {
			a.writeVarInt(0x00);
			a.writeVarInt(754);
			a.writeString(this.host.getHostString());
			a.writeShort(this.host.getPort());
			a.writeVarInt(2);
		});
		
		this.sendOnePacket(a -> {
			a.writeVarInt(0x00);
			a.writeString(this.name);
		});
	}
	
	public Bot disconnect() {
		this.channel.close();
		
		return this;
	}
	
	public Bot sendOnePacket(Consumer<McByteBuf> bytes) {
		var b = new McByteBuf(this.alloc().buffer());

		bytes.accept(b);
		this.send(b);
		
		return this;
	}
	
	public Bot registerListener(Consumer<SimplePacket> packet) {
		this.listener = packet;
		
		return this;
	}
	
	public Bot registerException(Consumer<Throwable> packet) {
		this.exception = packet;
		
		return this;
	}
	
	public Bot registerDisconnection(Consumer<Bot> packet) {
		this.disconnected = packet;
		
		return this;
	}
	
	public Bot unregisterListener() {
		this.listener = a -> {};
		
		return this;
	}
	
	public Bot unregisterException() {
		this.exception = a -> {};
		
		return this;
	}
	
	public Bot unregisterDisconnection() {
		this.disconnected = a -> {};
		
		return this;
	}
	
	public Integer getState() {
		return this.getChannel().attr(Bot.STATE).get();
	}

	public boolean atHandshakingState() {
		return this.getState() == 1;
	}
	
	public boolean atLoginState() {
		return this.getState() == 2;
	}
	
	public boolean atPlayState() {
		return this.getState() == 3;
	}
	
	public Integer getCompression() {
		return this.getChannel().attr(Bot.COMPRESSION).get();
	}
	
	public boolean hasCompression() {
		return this.getCompression() > 0;
	}
	
	public InetSocketAddress getHost() {
		return this.host;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Channel getChannel() {
		return this.channel;
	}
	
	private ChannelFuture send(McByteBuf buffer) {
		return this.channel.writeAndFlush(buffer);
	}
	
	private ByteBufAllocator alloc() {
		return this.channel.alloc();
	}
	
	public Bot.BotPilot getPilot() {
		if (this.pilot == null) this.pilot = new Bot.BotPilot();
	
		return this.pilot;
	}
	
	public void removePilot() {
		this.pilot = null;
	}
	
	public class BotPilot {
		private volatile boolean running = false;
		private volatile List<Consumer<McByteBuf>> actions = new ArrayList<>();
		
		private BotPilot() {}

		private void execute() {
			if (!BotPilot.this.actions.isEmpty()) {
				McByteBuf b = new McByteBuf(this.getBot().alloc().buffer());
				Consumer<McByteBuf> c = this.getActions().remove(0);
				c.accept(b);
				
				if (!(c instanceof Bot.Delay)) {
					this.getBot().send(b).addListener(new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture channel) {
							if (channel.isSuccess()) BotPilot.this.execute();
							else Bot.this.exception.accept(channel.cause());
						}
					});
				}
			}
			else BotPilot.this.running = false;
		}
		
		private void start() {
			if (!this.running) {
				this.running = true;
				this.execute();
			}
		}
		
		private synchronized List<Consumer<McByteBuf>> getActions() {
			return this.actions;
		}
		
		public Bot getBot() {
			return Bot.this;
		}
		
		public BotPilot queuePacket(Consumer<McByteBuf> a) {
			this.getActions().add(a);
			this.start();
			
			return this;
		}

		public BotPilot delay(long mili) {
			this.getActions().add(new Bot.Delay() {
				@Override
				public void accept(McByteBuf arg0) {
					Bot.MAIN_LOOP.schedule(() -> {
						BotPilot.this.execute();
					}, mili, TimeUnit.MILLISECONDS);
				}
			});
			this.start();
			
			return this;
		}
		
		public BotPilot chat(String message) {
			this.queuePacket(a -> {
				a.writeVarInt(0x03);
				a.writeString(message);
			});
			
			return this;
		}
		
		public BotPilot setHeldItem(int index) {
			this.queuePacket(a -> {
				a.writeVarInt(0x25);
				a.writeShort(index);
			});

			return this;
		}
		
		public BotPilot useMainHand() {
			this.queuePacket(a -> {
				a.writeVarInt(0x2F);
				a.writeVarInt(0);
			});
			
			return this;
		}
		
		public BotPilot useOffHand() {
			this.queuePacket(a -> {
				a.writeVarInt(0x2F);
				a.writeVarInt(1);
			});
			
			return this;
		}
		
		public BotPilot startSneaking() {
			this.queuePacket(a -> {
				a.writeVarInt(0x1C);
				a.writeVarInt(1);
				a.writeVarInt(0);
				a.writeVarInt(0);
			});
			
			return this;
		}
		
		public BotPilot stopSneaking() {
			this.queuePacket(a -> {
				a.writeVarInt(0x1C);
				a.writeVarInt(1);
				a.writeVarInt(1);
				a.writeVarInt(0);
			});
			
			return this;
		}
	}
	
	private interface Delay extends Consumer<McByteBuf> {}
}
