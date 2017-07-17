package gossip_protocol.src;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;

public class Node {

	private ExecutorService _executor = null;
	private String _address = null;
	private int _port = -1;
	private ServerSocket _serverSocket = null;
	private Socket _gossiperSocket = null;
	private ArrayList<String> _alliances = null;
	private ListenerThread _listenerThread = null;
	private GossiperThread _gossiperThread = null;

	public Node(String address) {
		initializeNode();
		parseAddress(address);
	}

	private void initializeNode() {
		_executor = Executors.newCachedThreadPool();
		_alliances = new ArrayList<String>();
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
	    		_listenerThread = new ListenerThread(_executor, _serverSocket);
	    	} 
	    	tListenerThread = new Thread(_listenerThread);
			tListenerThread.start();

			//Gossiper thread to broadcast gossip messages
			Thread tGossiperThread;
			if (_gossiperThread == null) {
				_gossiperThread = new GossiperThread();
			} 
			tGossiperThread = new Thread(_gossiperThread);
			tGossiperThread.start();

			//Wait for threads to finish
			try {
				tListenerThread.join();
				tGossiperThread.join();
			} catch (InterruptedException e) {

			}
		
		} catch (IOException e) {

		} finally {
			try {
				if (_serverSocket != null) {
					_serverSocket.close();
				}
				if (_gossiperSocket != null) {
					_gossiperSocket.close();
				}
				if (_executor != null) {
					_executor.shutdown();
				}
			} catch (IOException e) {

			}
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