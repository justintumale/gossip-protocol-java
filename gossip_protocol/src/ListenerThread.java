package gossip_protocol.src;

import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.io.BufferedReader;
import java.io.IOException;

public class ListenerThread implements Runnable {

	private ExecutorService _executor = null;
	private ServerSocket _serverSocket = null;
	private BufferedReader _br = null;
	protected ArrayList<String> alliances;

	public ListenerThread(ExecutorService executor, ServerSocket serverSocket) {
		_executor = executor;
		_serverSocket = serverSocket;
	}

	public void run() {
		System.out.println("Starting listener thread...");
		try {
			while(true) {
				try {
					Socket listenerSocket = _serverSocket.accept();
					System.out.println("Server connection accepted.");
					_executor.execute(new WorkerThread(listenerSocket, _serverSocket));
				} catch (SocketException e) {
					e.printStackTrace();
					break;
				}
			}	
			System.out.println("Finished listener thread.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}