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
	    	Thread listenerThread = new Thread(new ListenerThread(_executor, _serverSocket));
			listenerThread.start();

			//Gossiper thread to broadcast gossip messages
			Thread gossiperThread = new Thread(new GossiperThread());
			gossiperThread.start();

			//Wait for threads to finish
			try {
				listenerThread.join();
				gossiperThread.join();
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

    public static void main(String[]args) {
		Node n = new Node(args[0]);
		n.run();
    }
    

}