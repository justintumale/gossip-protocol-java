package gossip_protocol;

import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

public class WorkerThread implements Runnable {

	private String _command;
	private String _nodeAddress;
	private Socket _outgoingSocket;

    public WorkerThread(String command) {
    	_command = command;
    }

    public void run() {
    	String[] parsedCommand = parseCommand();
		dispatchCommand(parsedCommand);
    }

    private String[] parseCommand() {
    	//remove extra white spaces
    	return _command.split(" ");
    }

    private void dispatchCommand(String[] parsedCommand) {
    	switch(_command) {
    		case "connect":
    			connect();
    			break;
    		case "fail":
    			fail();
    			break;
    		case "gracefulShutdown":
    			gracefulShutdown();
    			break;
    		default:
    			break;
    	}
    }

    private void connect() {
    	System.out.println("Connect");
    	try {
    		_outgoingSocket = new Socket("127.0.0.1", 4000);
    		_outgoingSocket.close();
    	} catch (IOException e) {

    	} 
    }

    private void fail() {
    	System.out.println("Fail");
    }

    private void gracefulShutdown() {
    	System.out.println("Graceful shutdown");
    }

}