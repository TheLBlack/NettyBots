package mc.thelblack.bots.jbwm;

public enum Server {

	SURVIVAL(29), MINEZ(13);
	
	public short compass_slot;
	
	private Server(int slot) {
		this.compass_slot = (short) slot;
	}
}
