package com.b6w7.eece411.Server;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import com.b6w7.eece411.ChatMessage;
import com.b6w7.eece411.ClientInterface;
import com.b6w7.eece411.ServerInterface;

public class ChatServer extends UnicastRemoteObject
						implements ServerInterface {

	public static void main(String[] args) {
		try {
			Naming.rebind ("SHello", 
					new ChatServer ("Hello!"));
			System.out.println ("ChatServer is ready.");
		} catch (Exception e) {
			System.out.println ("ChatServer failed: " + e);
		}
	}

	private List<ChatMessage> messageList;
	private String user;
	private Vector<ClientInterface> clientList;
	
	public ChatServer(String user) throws RemoteException{
		this.user = user;
		//this.queue = LinkedList<ChatMessage>();
		this.messageList = Collections.synchronizedList(new LinkedList<ChatMessage>());
		this.clientList = new Vector<ClientInterface>();
	}
	
	@Override
	public void postMessage(ChatMessage msg) throws RemoteException {
		try{
			System.out.println("Received: "+msg.message());
			messageList.add(msg);	
		
			//TODO: Reply to all other Clients:
			// replyToClientGUI(msg);
			
			int i = 0;
			
			Iterator<ClientInterface> clientIterator = clientList.iterator();
			while( clientIterator.hasNext() ){
				ClientInterface aClient = clientIterator.next();
				aClient.replyToClientGUI( msg );
				System.out.println("Replied to " + aClient.getUsername() + " (Iterations of List: "+ ++i +") (Total clients "+ clientList.size() + ").");
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void register(ClientInterface client) throws RemoteException{
		if( !isRegistered(client) ){
			clientList.add(client);
			System.out.println("Registered client " + client.getUsername() +".");
		//	return true;
		}
		else{
			System.out.println("Client already registered.");
		//	return false;
		}
	}
	
	@Override
	public boolean isRegistered(ClientInterface client) throws RemoteException{
		return clientList.contains(client);
	}


}
