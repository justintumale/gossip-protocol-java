package gossip_protocol.src;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Node {

	private ExecutorService _executor = null;
	private String _address = null;
	private int _port = -1;
	private ServerSocket _serverSocket = null;
	private ListenerThread _listenerThread = null;
	private GossiperThread _gossiperThread = null;
	private ArrayList<Member> _alliances = null;
	private Member _thisMember = null;
	private final AtomicBoolean _isHealthy = new AtomicBoolean(true);

	public Node(String address) {
		parseAddress(address);
		initializeNode();
	}

	private void initializeNode() {
		_executor = Executors.newCachedThreadPool();
		_alliances = new ArrayList<Member>();
		_thisMember = new Member(_address, _port);
		_alliances.add(_thisMember);
	}

	private void parseAddress(String address) {
		if (address == null || address.trim().equals("")) {
			throw new IllegalArgumentException("Please enter a valid address in the proper format: <address>:<port>");
		}
		address = address.replaceAll(" +", " ");
		String[] addrArr = address.split(":");
		_address = addrArr[0];
		_port = Integer.parseInt(addrArr[1]);
	}

    public void run() {

    	try {
    		_serverSocket = new ServerSocket(_port);

	    	//Listener thread for incoming messages
	    	Thread tListenerThread;
	    	if (_listenerThread == null) {
	    		_listenerThread = new ListenerThread(_executor, _serverSocket, _isHealthy);
	    	} 
	    	tListenerThread = new Thread(_listenerThread);
			tListenerThread.start();

			//Gossiper thread to broadcast gossip messages
			Thread tGossiperThread;
			if (_gossiperThread == null) {
				_gossiperThread = new GossiperThread(_thisMember, _alliances, _isHealthy);
			} 
			tGossiperThread = new Thread(_gossiperThread);
			tGossiperThread.start();

			//Wait for threads to finish
			try {
				tListenerThread.join();
				tGossiperThread.join();

				System.out.println("Shutting down threads.");
				_serverSocket.close();
				_executor.shutdown();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		} 
    }

    public void attachThreadAndRun(ListenerThread listenerThread) throws IOException {
    	if (_listenerThread != null) {
    		throw new UnsupportedOperationException("A ListenerThread already exists for this thread.");
    	}
    	_listenerThread = listenerThread;
    	_listenerThread.alliances = _alliances;

		Thread tListenerThread = new Thread(_listenerThread);
		tListenerThread.start();
    }

    public void attachThreadAndRun(GossiperThread gossiperThread) throws IOException {
    	if (_gossiperThread != null) {
    		throw new UnsupportedOperationException("A GossiperThread already exists for this thread.");
    	}

    	_gossiperThread = gossiperThread;
    	_gossiperThread.alliances = _alliances;

		Thread tGossiperThread = new Thread(_gossiperThread);
		tGossiperThread.start();  
    }

    public static void main(String[]args) {
		Node n = new Node(args[0]);
		n.run();
    }
    

}