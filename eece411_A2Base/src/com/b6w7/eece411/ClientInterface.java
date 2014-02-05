package com.b6w7.eece411;

import java.rmi.*;

public interface ClientInterface extends Remote {
	/*
	 * Remotely invocable method,
	 * returns a message from the remote object, 
	 * throws a RemoteException 
	 *      if the remote invocation fails
	 */
	public void replyToClientGUI(ChatMessage answer) throws RemoteException;
	
	public String getUsername() throws RemoteException;
	
}
