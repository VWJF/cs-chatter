package com.b6w7.eece411;

import java.rmi.*;

public interface ClientInterface extends Remote {
	/**
	 * send a message to the client
	 * @param answer the message from server
	 * @throws RemoteException if the remote invocation fails
	 */
	public void replyToClientGUI(ChatMessage answer) throws RemoteException;
	
	/**
	 * get the client's username
	 * @return the client's username
	 */
	public String getUsername() throws RemoteException;
}
