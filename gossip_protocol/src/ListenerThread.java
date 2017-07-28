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

	public ListenerThread(ExecutorService executor, ServerSocket serverSocket, ArrayList<Member> alliances, AtomicBoolean isHealthy) {
		_executor = executor;
		_serverSocket = serverSocket;
		this.alliances = alliances;
		_isHealthy = isHealthy;
	}

	public void run() {
		Logger.info("Starting listener thread...");
		try {
			while(true) {
				try {
					Socket listenerSocket = _serverSocket.accept();
					Logger.info("Server connection accepted.");
					_executor.execute(new WorkerThread(listenerSocket, _serverSocket, alliances, _isHealthy));
				} catch (SocketException e) {
					e.printStackTrace();
					break;
				}
			}	
			Logger.info("Finished listener thread.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}