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
    	Thread t1 = new Thread(new ListenerThread());
		t1.start();
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
				_executor.execute(new WorkerThread(input));
				Thread.sleep(100);
				System.out.print(">");
		    }
		    t1.join();
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