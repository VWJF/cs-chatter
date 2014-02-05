/**
 * 
 */
package com.b6w7.eece411.Client;

import com.matei.eece411.GUI.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

import com.b6w7.eece411.ChatMessage;
import com.b6w7.eece411.ClientInterface;

/**
 * @author 
 *
 */
public class ChatClient extends UnicastRemoteObject
					implements ClientInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static int userCounter = 0;
	
	private String username;

	/**
	 * 
	 */
	public ChatClient() throws RemoteException {
		// TODO Auto-generated constructor stub

		//In order to create (semi-)unique user-names for several instances of the client. 
		//Used for testing.
		int userID =  (++userCounter) % 10;
		this.username = Integer.toString( (int)userID );
	}
	
	public String getUsername() throws RemoteException{
		return username;
	}
	

	/* (non-Javadoc)
	 * @see com.b6w7.eece411.ClientInterface#replyToClientGUI()
	 */
	@Override
	public void replyToClientGUI(ChatMessage answer) throws RemoteException {
		// TODO Auto-generated method stub
		System.out.println("Received reply: " + answer.message() );
		Main.gui.addToTextArea( answer.message() );
	}

}
