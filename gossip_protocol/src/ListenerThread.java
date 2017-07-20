package gossip_protocol.src;

import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ListenerThread implements Runnable {

	private ExecutorService _executor = null;
	private ServerSocket _serverSocket = null;
	private BufferedReader _br = null;
	protected ArrayList<Member> alliances;
	private final AtomicBoolean _isHealthy;

	public ListenerThread(ExecutorService executor, ServerSocket serverSocket, AtomicBoolean isHealthy) {
		_executor = executor;
		_serverSocket = serverSocket;
		_isHealthy = isHealthy;
	}

	public void run() {
		System.out.println("Starting listener thread...");
		try {
			while(true) {
				try {
					Socket listenerSocket = _serverSocket.accept();
					System.out.println("Server connection accepted.");
					_executor.execute(new WorkerThread(listenerSocket, _serverSocket, _isHealthy));
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