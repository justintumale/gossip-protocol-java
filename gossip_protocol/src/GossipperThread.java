package gossip_protocol.src;

import java.util.*;
import java.util.Random;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class GossipperThread implements Runnable {

	protected HashMap<String, Member> alliances;
	private Member _thisMember;
	private Socket _gossipperSocket;
	private PrintWriter _out;
	private final AtomicBoolean _isHealthy;
	private long _timeout;

	public GossipperThread(Member thisMember, HashMap<String, Member> alliances, AtomicBoolean isHealthy) {
		_thisMember = thisMember;
		this.alliances = alliances;
		_isHealthy = isHealthy;
		_timeout = 5000;
	}

	public String createGossipDigest() {
		StringBuilder digestBuilder = new StringBuilder();
		digestBuilder.append("gossip ");
		/*
		for (Member member : alliances) {
			digestBuilder.append(member.toString() + "-");
		}
		*/
		synchronized(this) {
			for (Map.Entry<String, Member> entry : alliances.entrySet()) {
				Member member = entry.getValue();
				digestBuilder.append(member.toString() + "-");
			}
		}
		return  digestBuilder.toString() + "_" + _thisMember.getAddress() + ":" + String.valueOf(_thisMember.getPort());
	}

	public void sendGossip() {
		_thisMember.incrementHeartbeat();

		//fail nodes or remove dead nodes
		cleanMembershipList();

		String gossipDigest = createGossipDigest();
		Logger.info("GOSSIP DIGEST CREATED " + gossipDigest);

		try {

			//TODO create a configuration file that reads in how many nodes to randomly gossip to periodically

			//get the live nodes
			HashSet<Member> liveNodes = getLiveNodes();

			HashSet<Integer> randomSet = new HashSet<Integer>();

			if (liveNodes.size() == 1) {
				randomSet.add(0);
			} else if (liveNodes.size() == 2) {
				randomSet.add(0);
				randomSet.add(1);
			} else if (liveNodes.size() == 3) {
				randomSet.add(0);
				randomSet.add(1);
				randomSet.add(2);
			} else {
				//TODO better implementation. random indices may end up being the same.
				Random rand = new Random();
				int index1 = rand.nextInt(liveNodes.size());
				int index2 = rand.nextInt(liveNodes.size());
				int index3 = rand.nextInt(liveNodes.size()); 
				randomSet.add(index1);
				randomSet.add(index2);
				randomSet.add(index3);
			}

			int selector = 0;
			synchronized(this) {
				Iterator<Member> iterator = liveNodes.iterator();
				while (iterator.hasNext()) {
					if (randomSet.contains(selector)){
						Member member = iterator.next();
						_gossipperSocket = new Socket(member.getAddress(), member.getPort());
						_out = new PrintWriter(_gossipperSocket.getOutputStream());
						_out.println(gossipDigest);
						_out.close();
					}
					selector++;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void cleanMembershipList() {
		HashSet<Member> toRemove = null;
		synchronized(this) {
			for (Map.Entry<String, Member> entry : alliances.entrySet()) {
				Member member = entry.getValue();
				if (System.currentTimeMillis() - member.getLocalTime() > (_timeout*2)) {
					if (toRemove == null) {
						toRemove = new HashSet<Member>();
					}
					toRemove.add(entry.getValue());
				} else if (System.currentTimeMillis() - member.getLocalTime() > _timeout) {
					member.setFail(true);
				}
			}

			if (toRemove != null) {
				Iterator<Member> iterator = toRemove.iterator();
				while(iterator.hasNext()) {
					Member member = iterator.next();
					alliances.remove(member.getAddress() + ":" + member.getPort());
				}
			}
		}
	}

	private HashSet<Member> getLiveNodes() {
		HashSet<Member> liveNodes = new HashSet<Member>();
		synchronized(this) {
			for (Map.Entry<String, Member> entry : alliances.entrySet()) {
				Member member = entry.getValue();
				if (!member.isFailed()) {
					liveNodes.add(member);
				}
			}
		}
		return liveNodes;
	}

	public void run() {
		Logger.info("Starting gossiper thread...");
		try {
			while (_isHealthy.get()) {
				Thread.sleep(3000);
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