package gossip_protocol.src;

import java.util.*;

public class GossiperThread implements Runnable {

	protected ArrayList<String> alliances;

	public GossiperThread() {

	}

	public String createGossipDigest() {
		return null;
	}

	public void sendGossip() {

	}

	public void run() {
		System.out.println("Starting gossiper thread...");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Finished gossiper thread.");
	}

}