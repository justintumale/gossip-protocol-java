package gossip_protocol.src;

import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

public class WorkerThread implements Runnable {

	private String _command;
	private String _nodeAddress;
	private Socket _outgoingSocket;
    private Socket _listenerSocket;
    private ServerSocket _serverSocket;
    private BufferedReader _br;

    public WorkerThread(Socket listenerSocket, ServerSocket serverSocket) {
    	_listenerSocket = listenerSocket;
        _serverSocket = serverSocket;
    }

    public void run() {
        try {
            System.out.println("Starting a worker thread...");
            _br = new BufferedReader(new InputStreamReader(_listenerSocket.getInputStream()));
            while (true) {
                _command = _br.readLine();
                System.out.println("Server received command from user: " + _command);
                String[] parsedCommand = parseCommand();
                if (parsedCommand[0].equalsIgnoreCase("q") || parsedCommand[0].equalsIgnoreCase("quit") || parsedCommand[0].equalsIgnoreCase("exit")) {
                    break;
                }
                dispatchCommand(parsedCommand);
            }
            System.out.println("Finished worker thread.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] parseCommand() {
    	//remove extra white spaces
        _command = _command.replaceAll(" +", " ").toLowerCase();
    	return _command.split(" ");
    }

    private void dispatchCommand(String[] parsedCommand) {
    	switch(parsedCommand[0]) {
    		case "connect":
                if (parsedCommand.length > 1) {
    			    connect(parsedCommand[1]);
                } else {
                    System.out.println("Please specify IP address to connect to.");
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
    		default:
    			break;
    	}
    }

    private void connect(String address) {
        String[]addressAndPort = address.split(":");
        if (addressAndPort.length != 2) {
            System.out.println("Please specify address and port in the proper format \"<address>:<port>\"");
            return;
        }
        
        address = addressAndPort[0];
        int port = Integer.valueOf(addressAndPort[1]);
        
        System.out.println("Connecting to " + address + ":" + port);
    	try {
    		_outgoingSocket = new Socket(address, port);
    		_outgoingSocket.close();
    	} catch (IOException e) {

    	} 
    }

    private void fail() {
    	System.out.println("Failing node...");
        try {
            _listenerSocket.close();
            _serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void gracefulShutdown() {
    	System.out.println("Graceful shutdown");
        //Same as fail?
    }

    private void testMethod() {
        System.out.println("test...");
    }

}