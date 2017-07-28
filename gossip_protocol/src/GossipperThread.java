package gossip_protocol.src;

import java.util.*;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class GossipperThread implements Runnable {

	protected ArrayList<Member> alliances;
	private Member _thisMember;
	private Socket _gossipperSocket;
	private PrintWriter _out;
	private final AtomicBoolean _isHealthy;

	public GossipperThread(Member thisMember, ArrayList<Member> alliances, AtomicBoolean isHealthy) {
		_thisMember = thisMember;
		this.alliances = alliances;
		_isHealthy = isHealthy;
	}

	public String createGossipDigest() {
		StringBuilder digestBuilder = new StringBuilder();
		digestBuilder.append("gossip ");
		for (Member member : alliances) {
			digestBuilder.append(member.toString() + "-");
		}
		return digestBuilder.toString();
	}

	public void sendGossip() {
		_thisMember.updateHeartbeat();

		String gossipDigest = createGossipDigest();
		Logger.info("gossip digest created: " + gossipDigest);
		Collections.shuffle(alliances);

		try {
			for (int i = 0; i < alliances.size(); i++) {
				if (i > 3) { break; }
				_gossipperSocket = new Socket(alliances.get(i).getAddress(), alliances.get(i).getPort());
				_out = new PrintWriter(_gossipperSocket.getOutputStream());
				_out.println(gossipDigest);
				_out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		Logger.info("Starting gossiper thread...");
		try {
			while (_isHealthy.get()) {
				Thread.sleep(10000);
				Logger.info("Sending gossip...");
				sendGossip();
				Logger.info("Gossip sent");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Logger.info("Finished gossiper thread.");
	}

}