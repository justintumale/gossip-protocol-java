package gossip_protocol.src;

import java.util.*;

public class Member {
    private String _address;
    private int _port;
    private long _heartbeat;
    private long _localTime;

    public Member() {
	_heartbeat = 0;
    }

    public Member(String address, int port) {
	_address = address;
	_port = port;
	_heartbeat = 0;
    }

    public void setAddress(String address) {
	_address = address;
    }

    public String getAddress() {
	return _address;
    }

    public void setPort(int port) {
	_port = port;
    }

    public int getPort() {
	return _port;
    }

    public void updateHeartbeat() {
	_heartbeat++;
    }

    public long getHeartbeat() {
	return _heartbeat;
    }

    public void setLocalTime(long localTime) {
	_localTime = localTime;
    }

    public long  getLocalTime() {
	return _localTime;
    }

    public String toString() {
        return _address + ":" + String.valueOf(_port) + ":" + String.valueOf(_heartbeat);
    }

}