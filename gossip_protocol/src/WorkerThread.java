package gossip_protocol.src;

import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorkerThread implements Runnable {

	private String _command;
	private String _nodeAddress;
	private Socket _outgoingSocket;
    private Socket _listenerSocket;
    private ServerSocket _serverSocket;
    private BufferedReader _br;
    private final AtomicBoolean _isHealthy;
    protected ArrayList<Member> _alliances;



    public WorkerThread(Socket listenerSocket, ServerSocket serverSocket, ArrayList<Member> alliances, AtomicBoolean isHealthy) {
    	_listenerSocket = listenerSocket;
        _serverSocket = serverSocket;
        _alliances = alliances;
        _isHealthy = isHealthy;
    }

    public void run() {
        try {
            Logger.info("Starting a worker thread...");
            while(true) {
                _br = new BufferedReader(new InputStreamReader(_listenerSocket.getInputStream()));
                _command = _br.readLine();
                Logger.info("Server received command from user: " + _command);
                String[] parsedCommand = parseCommand();
                if (parsedCommand[0].equalsIgnoreCase("q") || parsedCommand[0].equalsIgnoreCase("quit") || parsedCommand[0].equalsIgnoreCase("exit")) {
                        dispatchCommand(new String[]{"fail"});
                        break;
                }
                dispatchCommand(parsedCommand);
                if (parsedCommand[0].equals("gossip")) {
                    break;
                }
            }
            Logger.info("Finished worker thread.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] parseCommand() {
    	//remove extra white spaces
        if (_command == null) {
            return null;
        }
        _command = _command.replaceAll(" +", " ").toLowerCase();
    	return _command.split(" ");
    }

    private void dispatchCommand(String[] parsedCommand) {
    	switch(parsedCommand[0]) {
    		case "connect":
                if (parsedCommand.length > 1) {
    			    connect(parsedCommand[1]);
                } else {
                    Logger.error("Please specify IP address to connect to.");
                }
    			break;
    		case "fail":
    			fail();
    			break;
    		case "gracefulShutdown":
    			gracefulShutdown();
    			break;
            case "test":
                testMethod();
                break;
            case "gossip":
                receiveGossip(parsedCommand[1]);
                break;
    		default:
    			break;
    	}
    }

    private void connect(String address) {
        String[]addressAndPort = address.split(":");
        if (addressAndPort.length != 2) {
            Logger.error("Please specify address and port in the proper format \"<address>:<port>\"");
            return;
        }
        
        address = addressAndPort[0];
        int port = Integer.valueOf(addressAndPort[1]);
        
        Logger.info("Connecting to " + address + ":" + port);
    	try {
    		_outgoingSocket = new Socket(address, port);
    		_outgoingSocket.close();
    	} catch (IOException e) {

    	} 
    }

    private void fail() {
    	Logger.info("Failing node...");
        try {
            _listenerSocket.close();
            _serverSocket.close();
            _isHealthy.set(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void gracefulShutdown() {
    	Logger.info("Graceful shutdown");
        //Same as fail?
    }

    private void testMethod() {
        Logger.info("test...");
    }

    private void receiveGossip(String gossipDigest) {
        String[] memberArray = gossipDigest.split("-");
        Logger.info("Gossip message received:   ");
        for (int i = 0; i < memberArray.length; i++) {
            Logger.info("        " + memberArray[i]);
        }
        //mergeGossipDigest(gossipDigest);
    }

    private void mergeGossipDigest(String gossipDigest) {

    }

}