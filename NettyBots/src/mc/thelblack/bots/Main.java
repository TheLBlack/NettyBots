package mc.thelblack.bots;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;
import java.util.zip.DataFormatException;

import mc.thelblack.bots.jbwm.JBot;
import mc.thelblack.bots.jbwm.Job;
import mc.thelblack.bots.jbwm.NameProvider;
import mc.thelblack.bots.jbwm.Server;

public class Main {

	public static void main(String[] args) {
		var bot = new JBot("testttt");
		bot.connect();
		
		bot.registerListener(packet -> {
			try {
				packet.decompressIfNeeds();
			} catch (IOException | DataFormatException e) {
				e.printStackTrace();
			}
			if (packet.getPacketId() == 0x13) {
				System.out.println("---");
				System.out.println(packet.getByteBuf().readVarInt());
				System.out.println(packet.getByteBuf().readShort());
				byte[] bytes = new byte[packet.getByteBuf().readableBytes()];
				packet.getByteBuf().readBytes(bytes);
				for (byte bbb : bytes) {
					System.out.println(bbb);
				}
			}
			else if (packet.getPacketId() == 0x0E) {
				bot.getPilot().delay(2000).chat("/login dupa1234").delay(6000).setHeldItem(4).useMainHand();
			}
		});
	}
	
	public static void main3(String[] args) {
		while (true) {
			System.out.println("newjob / removejob / listjobs / end / loadnames");
			String line = System.console().readLine();
			
			if (line != null) {
				String[] c = line.split(" ", 2);
				
				if (c[0].equalsIgnoreCase("newjob") && c.length > 1) {
					try {
						String[] ar = c[1].split(" ", 3);
						Server s = Server.values()[Integer.parseInt(ar[0])];
						int target = Integer.parseInt(ar[1]);
						int hours = Integer.parseInt(ar[2]);
						
						new Job(s, target, hours);
						System.out.println("job created");
					}
					catch (NumberFormatException | IndexOutOfBoundsException | NullPointerException e) {
						System.out.println("incorrect input: newjob [server id] [target] [hours]");
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				else if (c[0].equalsIgnoreCase("removejob") && c.length > 1) {
					try {
						int index = Integer.parseInt(c[1]);
						Job j = Job.getJobs().get(index);
						
						j.end = !j.end;
						System.out.println("ending: " + j.end);
					}
					catch (NumberFormatException | IndexOutOfBoundsException e) {
						System.out.println("incorrect index");
					}
				}
				else if (c[0].equalsIgnoreCase("listjobs")) {
					List<Job> j = Job.getJobs();
					
					System.out.println("jobs: (size: " + j.size() + ")");
					IntStream.range(0, j.size()).forEachOrdered(a -> {
						System.out.println(String.format("%d. %s", a, j.get(a).toString()));
					});
				}
				else if (c[0].equalsIgnoreCase("end")) {
					Job.FORCE_ENDING = !Job.FORCE_ENDING;
					System.out.println("force ending: " + Job.FORCE_ENDING);
				}
				else if (c[0].equalsIgnoreCase("loadnames") && c.length > 1) {
					File f = new File(c[1]);
					if (!f.exists()) System.out.println("file not found");
					else {
						try {
							NameProvider.loadNames(f);
							System.out.println("loaded");
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	public static void main2(String[] args) throws InterruptedException {
		HashMap<String, Bot> bots = new HashMap<>();
		
		while (true) {
			System.out.println("join / leave / chat / list");
			String c = System.console().readLine();
			
			String[] l = c.split(" ", 3);
			if (l.length >= 2) {
				if (l[0].equalsIgnoreCase("join")) {
					var t = new Bot(new InetSocketAddress("panel.jbwm.pl", 26024), l[1]);
					
					t.registerDisconnection(a -> {
						System.out.println(a.getName() + ": connection lost");
					});
					
					t.registerException(a -> {
						System.out.println(t.getName() + ": exception");
						a.printStackTrace();
					});
					
					t.registerListener(a -> {
						try {
							a.decompressIfNeeds();
						} catch (IOException | DataFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						if (a.getPacketId() == 25) {
							System.out.println(t.getName() + ":");
							System.out.println("id: " + a.getPacketId());
							System.out.println("deco: " + a.isDecompressed());
							System.out.println(a.getByteBuf().readString());
						}
						
						if (a.getPacketId() == 0x2D) {
							t.sendOnePacket(b -> {
								b.writeVarInt(0x09);
								b.writeByte(1);
								b.writeShort(29);
								b.writeByte(0);
								b.writeShort(1);
								b.writeVarInt(4);
								b.writeBoolean(false);
							});
						}
					});
					
					System.out.println("joining...");
					t.connect();
					
					Thread.sleep(3000);
					
					t.sendOnePacket(a -> {
						a.writeVarInt(0x03);
						a.writeString("/login qweasd1");
					});
					
					Thread.sleep(3000);
					
					t.sendOnePacket(a -> {
						a.writeVarInt(0x25);
						a.writeShort(4);
					});
					
					Thread.sleep(4000);
					
					t.sendOnePacket(a -> {
						a.writeVarInt(0x2F);
						a.writeVarInt(0);
					});
					
					System.out.println("joined");
					bots.put(l[1], t);
					
					//t.getPilot().startSneaking().chat("sneaking...").delay(10000).stopSneaking().chat("stopped");
				}
				else if (l[0].equalsIgnoreCase("leave")) {
					Bot b = bots.get(l[1]);
					if (b != null) {
						b.disconnect();
						System.out.println("left");
					}
					
					bots.remove(l[1]);
				}
				else if (l[0].equalsIgnoreCase("chat")) {
					Bot b = bots.get(l[1]);
					if (b != null) {
						bots.get(l[1]).sendOnePacket(a -> {
							a.writeVarInt(0x03);
							a.writeString(l[2]);
						});
					}
				}
				else if (l[0].equalsIgnoreCase("list")) {
					System.out.println("size: " + bots.size());
					bots.keySet().forEach(System.out::println);
				}
			}
		}
	}
}
