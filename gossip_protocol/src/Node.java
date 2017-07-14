package gossip_protocol.src;

import java.util.*;
import java.util.concurrent.ExecutorService;  
import java.util.concurrent.Executors;


public class Node {

	private ExecutorService _executor = null;

	public Node() {
		initializeNode();
	}

	private void initializeNode() {
		_executor = Executors.newCachedThreadPool();
	}

    public void run() {

    	//Listener thread for incoming messages
    	Thread listenerThread = new Thread(new ListenerThread());
		listenerThread.start();

		//Gossiper thread to broadcast gossip messages
		Thread gossiperThread = new Thread(new GossiperThread());
		gossiperThread.start();

		Scanner in = null;
		try {
			Thread.sleep(100);
			System.out.print(">");
		    in = new Scanner(System.in);
		    while (in.hasNextLine()){
		        String input = in.nextLine();
				if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("q")) {
					break;
				}
				//Worker thread to handle commands
				_executor.execute(new WorkerThread(input));
				Thread.sleep(100);
				System.out.print(">");
		    }

		    //close out threads
		    listenerThread.join();
		    gossiperThread.join();
		    _executor.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
		    in.close();
		}
    }

    public static void main(String[]args) {
		Node n = new Node();
		n.run();
    }
    

}