package com.b6w7.eece411.Server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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

	private static void printUsage() {
		System.out.println("USAGE:\n"
				+ "  java -cp"
				+ " <file.jar>"
				+ " " + ChatServer.class.getCanonicalName() 
				+ " <registry URL>"  
				+ " [<registry port>]");
		System.out.println("EXAMPLE:\n"
				+ "  java -cp"
				+ " A2.jar"
				+ " " + ChatServer.class.getCanonicalName() 
				+ " localhost");
	}

	public static void main(String[] args) {
		String registryAddress;
		int registryPort;
		
		// If the command line arguments are missing, then nothing to do
		if ( args.length < 1 || args.length > 2 ) {
			printUsage();
			return;
		}

		try {
			// validate registry information from command line parameters 
			// validate host can be found and port is within range.
			// use default registry port if unspecified port.
			registryAddress = args[0];
			InetAddress.getByName(args[0]);
			if (args.length == 1)
				registryPort = Registry.REGISTRY_PORT;
			else
				registryPort = Integer.parseInt(args[1]);
			if (registryPort < 1024 || registryPort > 65535)
				throw new NumberFormatException();
			
		} catch (UnknownHostException e) {
			System.out.println("Unknown host for registry.");
			printUsage();
			return;
			
		} catch (NumberFormatException e1) {
			System.out.println("Registry port must be numerical digits between 1024 to 65535, inclusive.");
			printUsage();
			return;
		}

		try {
			Registry registry = LocateRegistry.getRegistry(registryAddress, registryPort);
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
