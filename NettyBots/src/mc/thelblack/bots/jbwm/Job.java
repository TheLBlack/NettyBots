package mc.thelblack.bots.jbwm;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;

public class Job {

	public static volatile boolean FORCE_ENDING = false;
	
	static {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		
		scheduler.scheduleAtFixedRate(() -> {
			new ArrayList<>(Job.JOBS).forEach(Job::beat);
			
			if (Job.FORCE_ENDING && Job.JOBS.isEmpty()) scheduler.shutdown();
		}, 10, 7, TimeUnit.SECONDS);
	}
	
	private static final String LOGIN_MESSAGE = "/login", REGISTER_MESSAGE = "/register", PASSWORD = "qweasd1";
	
	private static volatile List<Job> JOBS = new ArrayList<>();
	
	public static synchronized List<Job> getJobs() {
		return Job.JOBS;
	}
	
	private final Server server;
	private final int target;
	private final long lifetime;
	
	private volatile List<JBot> bots = new ArrayList<>();
	
	public volatile boolean end = false;
	
	public Job(Server server, int target, int hours) {
		this.server = server;
		this.target = target;
		this.lifetime = System.currentTimeMillis() + (hours*60*60*1000);
		
		Job.getJobs().add(this);
	}
	
	public void joinNewBot() {
		String name = NameProvider.getName();
		
		if (name != null) {
			var bot = new JBot(name);
			this.getBots().add(bot);
			
			bot.registerDisconnection(jbot -> {
				this.bots.remove(jbot);
				NameProvider.returnName(jbot.getName());
			});
			
			bot.connect();
			bot.registerListener(packet -> {
				try {
					packet.decompressIfNeeds();
				} catch (IOException | DataFormatException e) {
					e.printStackTrace();
				}
				if (packet.getPacketId() == 0x0E) {
					String message = packet.getByteBuf().readString();

					if (message.contains("Prosz") && message.contains(Job.LOGIN_MESSAGE)) bot.getPilot().delay(2000).chat("/login " + Job.PASSWORD);
					else if (message.contains("Prosz") && message.contains(Job.REGISTER_MESSAGE)) bot.getPilot().delay(2000).chat("/register " + Job.PASSWORD + " " + Job.PASSWORD);
					else if (message.contains("z powodu sesji")) bot.getPilot().delay(1000);
					else return;

					bot.getPilot().delay(6000).setHeldItem(4).useMainHand();
					bot.removePilot();
					
					bot.registerListener(packet2 -> {
						if (packet2.getPacketId() == 0x2D) {
							bot.sendOnePacket(b -> {
								b.writeVarInt(0x09);
								b.writeByte(1);
								b.writeShort(Job.this.server.compass_slot);
								b.writeByte(0);
								b.writeShort(1);
								b.writeVarInt(4);
								b.writeBoolean(false);
							});
							
							bot.registerListener(packet3 -> {
								if (packet3.getPacketId() == 25) {
									bot.disconnect();
								}
							});
						}
					});
				}
			});
			
			bot.registerException(cause -> {
				cause.printStackTrace();
			});
		}
		else System.out.println("Name provider is empty!");
	}
	
	public void leaveAsBot() {
		if (!this.bots.isEmpty()) this.getBots().remove(0).disconnect();
	}
	
	public void beat() {
		if (this.lifetime < System.currentTimeMillis() || Job.FORCE_ENDING || this.end) {
			if (!this.bots.isEmpty()) this.leaveAsBot();
			else Job.getJobs().remove(this);
		}
		else if (this.bots.size() < this.target) this.joinNewBot();
	}
	
	public synchronized List<JBot> getBots() {
		return this.bots;
	}
	
	@Override
	public String toString() {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		return String.format("[Server: %s Target: %d Bots: %d Lives to: (%d) %s Ending: %s]", this.server, this.target, this.getBots().size(), this.lifetime, format.format(this.lifetime), this.lifetime < System.currentTimeMillis() || Job.FORCE_ENDING || this.end);
	}
}
