package com.b6w7.eece411;

import java.rmi.*;

public interface HelloInterface extends Remote {
	/*
	 * Remotely invocable method,
	 * returns a message from the remote object, 
	 * throws a RemoteException 
	 *      if the remote invocation fails
	 */
	public String say() throws RemoteException;
}
