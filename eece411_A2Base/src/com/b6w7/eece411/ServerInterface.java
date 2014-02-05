package com.b6w7.eece411;

import java.rmi.*;

public interface ServerInterface extends Remote{
	
	public void postMessage(ChatMessage msg) throws RemoteException;
	
	//public void register(ClientInterface client) throws RemoteException;

	public void register(ClientInterface client, ChatMessage msg) throws RemoteException;
	
	public boolean isRegistered(ClientInterface client) throws RemoteException;
}
