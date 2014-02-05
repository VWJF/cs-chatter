package com.b6w7.eece411;
import java.rmi.*;
import java.rmi.server.*;

public class HelloImpl extends UnicastRemoteObject
implements HelloInterface {
	/**
	 * i
	 */
	private static final long serialVersionUID = 1L;
	private String message;
	/* Constructor for a remote object
	 * Throws a RemoteException if the object handle   
	 * cannot be constructed 
	 */
	public HelloImpl(String msg) throws RemoteException{
		message = msg;
	}
	/* Implementation of the remotely invocable method */
	public String say() throws RemoteException {
		return message;
	}
}
