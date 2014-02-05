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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

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
			e.printStackTrace();
			return;
		}
	}

	private List<ChatMessage> messageList;
	private ChatMessage _head, _tail, _watermark;
	private String user;
	//private List<ClientStructure> clientList;
	private Map<ClientInterface, ChatMessage> clientList;
	
	public ChatServer(String user) throws RemoteException{
		this.user = user;
		this.messageList = Collections.synchronizedList(new LinkedList<ChatMessage>());
//		this.clientList = Collections.synchronizedList(new LinkedList<ClientStructure>());
		this.clientList = Collections.synchronizedMap(new ConcurrentHashMap<ClientInterface, ChatMessage>());
		
		//messageList.add(new ChatMessage("","Connected!"));
		
		//updateWatermark();
	}
	
	private void updateWatermark(){
		
		if(_watermark == null) {
			synchronized(messageList){
				//messageList synchronized to make concurrently safe.
				_head = _tail = _watermark = messageList.get(0);
			}
		}

		synchronized(messageList){		
			System.out.println("Updating Watermark. Watermark Index:"+ (messageList.indexOf( _watermark )) +" Size: " + messageList.size());

			try{

				while( clientList.containsValue( _watermark ) )
					if( !messageList.isEmpty() ){
						_watermark = messageList.get( messageList.indexOf(_watermark) + 1 );
						System.out.println("Updated Watermark when messageList empty. Watermark Index:"+ (messageList.indexOf( _watermark )) +" Size: " + messageList.size());
					}
			}
			catch(IndexOutOfBoundsException ioof)
			{
				System.out.println("IndexOutOfBoundsException: Watermark Index : "+ (messageList.indexOf( _watermark ) +1) +" Size: " + messageList.size());
				System.out.println(ioof.getLocalizedMessage());
				_watermark = messageList.get( messageList.size()-1 );
			}
		}
	}

	@Override
	public void postMessage(ChatMessage msg) throws RemoteException {
		try{
			System.out.println("Received: "+msg.message());

			//Add received chat message to the master message List. 
			synchronized(messageList){
				messageList.add(msg);
				_tail = msg;
				System.out.println("Updating MessageList. MessageList Index:"+ (messageList.indexOf( msg )) +" Size: " + (messageList.size()));
			}
			sendMessage(msg);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public void sendMessage(ChatMessage msg) throws RemoteException {
		try{
			int i = 0;
			/*
			 * For every <ClientInterface, ChatMessage> Map entry, 
			 * reply with the ChatMessage to the gui in the ClientInterface.
			 * After sending to the client, update the ChatMessage in the pair <ClientInterface, ChatMessage> 
			 * with the next message from the message list. 
			 */
			synchronized(clientList){
				//	Iterator<ClientStructure> clientIterator = clientList.iterator();
				Iterator<Entry<ClientInterface, ChatMessage>> clientIterator = clientList.entrySet().iterator();

				while( clientIterator.hasNext() ){
					//ClientInterface aClient = clientIterator.next();
					Entry<ClientInterface, ChatMessage> aClient = clientIterator.next();
					//aClient.replyToClientGUI( msg );

					try{
						System.out.println("Updating ChatMessage associated to a ClientInterface.");
						aClient.setValue( messageList.get( messageList.indexOf( aClient.getValue() ) + 1 ));
					}
					catch(IndexOutOfBoundsException ioof)
					{
						System.out.println("IndexOutOfBoundsException:" + ioof.getLocalizedMessage() );
						System.out.println(aClient.getKey().getUsername() + " has sent all messages.");						
					}

					aClient.getKey().replyToClientGUI( aClient.getValue() );
					System.out.println("Replied to \"" + aClient.getValue().message()+"\" " + "with: " + "(Iterations of List: "+ ++i +") (Total clients "+ clientList.size() + ").");

				}
				updateWatermark();

			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void register(ClientInterface client, ChatMessage msg) throws RemoteException{
		if( !isRegistered(client) ){
			//clientList.add(client);
			clientList.put(client, msg);

			messageList.add(msg);
			
			System.out.println("Registered client " + client.getUsername() +".");
		}
		else{
			System.out.println("Client already registered.");
		}

		sendMessage(msg);

		//updateWatermark();	

	}

	@Override
	public boolean isRegistered(ClientInterface client) throws RemoteException{
		//return clientList.contains(client);
		return clientList.containsKey(client);

	}


}
