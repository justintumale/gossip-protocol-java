package gossip_protocol.src;

import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorkerThread implements Runnable {

	private String _command;
	private String _nodeAddress;

    /*Reference to the node's main server socket, for shut down purposes*/
    private ServerSocket _serverSocket;

    /*When a connection is made to the listener thread's server socket, this socket is spawned*/
    private Socket _listenerSocket;

    /*This socket is used for communicating with any alternative nodes besides the one the one responsible for spawning this thread*/
    private Socket _outgoingSocket;

    private BufferedReader _br;
    private final AtomicBoolean _isHealthy;
    protected ArrayList<Member> _alliances;
    private String _address;
    private int _port;

    public WorkerThread(Socket listenerSocket, ServerSocket serverSocket, ArrayList<Member> alliances, 
    AtomicBoolean isHealthy, String address, int port) {
    	_listenerSocket = listenerSocket;
        _serverSocket = serverSocket;
        _alliances = alliances;
        _isHealthy = isHealthy;
        _address = address;
        _port = port;
    }

    public void run() {
        try {
            Logger.info("Starting a worker thread...");
            while(true) {
                _br = new BufferedReader(new InputStreamReader(_listenerSocket.getInputStream()));
                _command = _br.readLine();
                Logger.info("Server received command: " + _command);
                String[] parsedCommand = parseCommand();
                if (parsedCommand[0].equalsIgnoreCase("q") || parsedCommand[0].equalsIgnoreCase("quit") || parsedCommand[0].equalsIgnoreCase("exit")) {
                        dispatchCommand(new String[]{"fail"});
                        break;
                }
                if (!dispatchCommand(parsedCommand)) {
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

    private boolean dispatchCommand(String[] parsedCommand) throws IOException {
    	switch(parsedCommand[0]) {
    		case "connect":
                if (parsedCommand.length > 1) {
    			    return connect(parsedCommand[1]);
                } else {
                    Logger.error("Please specify IP address to connect to.");
                    return true;
                }
            case "connection-request": 
                if (parsedCommand.length > 1) {
                    return recvConnectionRequest(parsedCommand[1]);
                } else {
                    Logger.error("Connection request could not be made.");
                    return true;
                }
            case "connection-request-ack": 
                if (parsedCommand.length > 1) {
                    return recvConnectionRequestACK(parsedCommand[1]);
                } else {
                    Logger.error("Connection request ACK could not be made.");
                    return true;
                }
    		case "fail":
    			return fail();
    		case "gracefulShutdown":
    			return gracefulShutdown();
            case "test":
                return testMethod();
            case "gossip":
                return receiveGossip(parsedCommand[1]);
    		default:
                return true;
    	}
    }

    private boolean connect(String address) throws IOException {
        //Parse address
        String[]addressAndPort = address.split(":");
        if (addressAndPort.length != 2) {
            Logger.error("Please specify address and port in the proper format \"<address>:<port>\"");
            return true;
        }
        address = addressAndPort[0];
        int port = Integer.valueOf(addressAndPort[1]);
        
        //Send a connection request to the desired node
        Logger.info("Connecting to " + address + ":" + port);
    	try {
    		_outgoingSocket = new Socket(address, port);
            Logger.info("Connection accepted by " + address + ":" + String.valueOf(port));
            PrintWriter out = new PrintWriter(_outgoingSocket.getOutputStream(), true);
            out.println("connection-request " + _address + ":" + _port);
    		_outgoingSocket.close();
    	} catch (IOException e) {
            e.printStackTrace();
    	} 
        return false;
    }

    private boolean recvConnectionRequest(String address) throws IOException {
        //TODO refactor
        String[]addressAndPort = address.split(":");
        if (addressAndPort.length != 2) {
            Logger.error("Please specify address and port in the proper format \"<address>:<port>\"");
            return true;
        }
        
        address = addressAndPort[0];
        int port = Integer.valueOf(addressAndPort[1]);

        //Add requesting node to memebership list
        addToMembershipList(address, port);

        //Send an ack to the node
        Logger.info("Sending an ACK to the requesting node " + address);
        _outgoingSocket = new Socket(address, port);
        PrintWriter out = new PrintWriter(_outgoingSocket.getOutputStream(), true);
        out.println("connection-request-ack " + _address + ":" + String.valueOf(_port));
        out.close();

        return false;
    }

    private boolean recvConnectionRequestACK(String address) throws IOException {
        Logger.info("Connect node request ACK received from " + address);

        //TODO refactor
        String[]addressAndPort = address.split(":");
        if (addressAndPort.length != 2) {
            Logger.error("Please specify address and port in the proper format \"<address>:<port>\"");
            return true;
        }
        
        address = addressAndPort[0];
        int port = Integer.valueOf(addressAndPort[1]);

        //Add requesting node to memebership list
        addToMembershipList(address, port);

        return false;
    }

    private boolean fail() {
    	Logger.info("Failing node...");
        try {
            _listenerSocket.close();
            _serverSocket.close();
            _isHealthy.set(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean gracefulShutdown() {
    	Logger.info("Graceful shutdown");
        //Same as fail?
        return false;
    }

    private boolean testMethod() {
        Logger.info("TEST.");
        return true;
    }

    private boolean receiveGossip(String gossipDigest) {
        String[] memberArray = gossipDigest.split("-");
        Logger.info("Gossip message received:   ");
        for (int i = 0; i < memberArray.length; i++) {
            Logger.info("        " + memberArray[i]);
        }
        //mergeGossipDigest(gossipDigest);
        return false;
    }

    private void mergeGossipDigest(String gossipDigest) {

    }

    private void addToMembershipList(String address, int port) {
        Logger.info("Adding " + address + ":" + port + " to alliances...");
        Member ally = new Member(address, port);
        synchronized(this) {
            _alliances.add(ally);
        }
        Logger.info(address + ":" + port + " successfully added to alliances.");
    }

}