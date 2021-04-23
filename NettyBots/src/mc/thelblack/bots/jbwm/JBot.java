package mc.thelblack.bots.jbwm;

import java.net.InetSocketAddress;

import mc.thelblack.bots.Bot;

public class JBot extends Bot {

	private static final InetSocketAddress IP = new InetSocketAddress("panel.jbwm.pl", 26024);
	
	public JBot(String name) {
		super(JBot.IP, name);
	}
}
