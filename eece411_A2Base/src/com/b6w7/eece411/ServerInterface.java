package com.b6w7.eece411;

import java.rmi.*;

public interface ServerInterface extends Remote{
	
	/**
	 * post message to server
	 * @param msg message to post
	 * @throws RemoteException if network error occurs
	 */
	public void postMessage(ChatMessage msg) throws RemoteException;

	/**
	 * register a {@link ChatClient} with server
	 * @param client client to register
	 * @throws RemoteException if network error occurs
	 */
	public void register(ClientInterface client) throws RemoteException;

	/**
	 * check is {@link ChatClient} is registered with server
	 * @param client client to check
	 * @return true if registered, false otherwise.
	 * @throws RemoteException if network error occurs
	 */
	public boolean isRegistered(ClientInterface client) throws RemoteException;
}
