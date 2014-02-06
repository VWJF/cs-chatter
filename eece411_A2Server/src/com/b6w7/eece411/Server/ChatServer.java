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
import java.util.HashSet;
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
	private ChatMessage _watermark;
	
	//private List<ClientStructure> clientList;
	private Map<ClientInterface, ChatMessage> clientList;
	private Set<ClientInterface> clientListStale;
	
	public ChatServer(String user) throws RemoteException{
		this.messageList = Collections.synchronizedList(new LinkedList<ChatMessage>());
		this.clientList = Collections.synchronizedMap(new ConcurrentHashMap<ClientInterface, ChatMessage>());
		this.clientListStale = Collections.synchronizedSet(new HashSet<ClientInterface>());

		//messageList.add(new ChatMessage("","Welcome to ChatRoom"));
		//_watermark = messageList.get(0);
		
		//updateWatermark();
	}
	
	private void updateWatermark(){
		
		if(_watermark == null) {
			synchronized(messageList){
				//messageList synchronized to make concurrently safe.
				_watermark = messageList.get(0);
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
/*
 * TODO:
 * The isStaleClients() should be executed every 10 seconds.
 * Every execution will check for Stale clients and remove them as necessary.
 * Untested.
 */
	public boolean isStaleClients(){
		return clientListStale.isEmpty() ? true : removeStaleClients() ; 
	}
	
	private boolean removeStaleClients(){
		
		return clientList.keySet().removeAll(clientListStale);
	}
	
	
	
	@Override
	public void postMessage(ChatMessage msg) throws RemoteException {

		System.out.println("Received: "+msg.message());

		//Add received chat message to the master message List. 
		synchronized(messageList){
			messageList.add(0, msg);  // add to front of list
			System.out.println("Updating MessageList. MessageList Index:"+ (messageList.indexOf( msg )) +" Size: " + (messageList.size()));
		}
		sendMessage();
	}

	private void sendMessage() {
			/*
			 * For every <ClientInterface, ChatMessage> Map entry, 
			 * reply with the ChatMessage to the gui in the ClientInterface.
			 * After sending to the client, update the ChatMessage in the pair <ClientInterface, ChatMessage> 
			 * with the next message from the message list. 
			 */
			synchronized(clientList){
				Iterator<Entry<ClientInterface, ChatMessage>> clientIterator = clientList.entrySet().iterator();

				while( clientIterator.hasNext() ){
					Entry<ClientInterface, ChatMessage> aClient = clientIterator.next();

					System.out.println("Updating ChatMessage associated to a ClientInterface.");
					
					// the referenced message of the client has already been sent successfully,
					// so find any messages proceeding it,
					// send those messages in order to the client,
					// and update the reference after each successful send.
					// When all messages for a client have been sent, 
					// iterate to the next client.
					// If any message fails to send, the reference will remain intact 
					// pointing to the last successfully sent message.
					ChatMessage msg = aClient.getValue();
					int index = messageList.indexOf( msg );
					ClientInterface client = aClient.getKey();

					try{
						while (index > 0) {
							msg = messageList.get( --index );
							client.replyToClientGUI( msg );
							System.out.println("Replied to \"" + msg.message()+"\""); 
							aClient.setValue( msg );
						}
						
					} catch(RemoteException e){
						System.out.println("Client not responsive.  Could not send message \"" + msg.message() + "\" to client.");
						// message failed -- do nothing.  Proceed to next client.
						
						clientListStale.add(client);
						System.out.println("Non-reponsive clients \"" + clientListStale.size() + "\".");
					}
				}
				System.out.println("(Total clients "+ clientList.size() + ").");
			}
			updateWatermark();
			System.out.println("Messages in the List: " + messageList.size());
	}

	//TODO: To be removed
	@Override
	public void register(ClientInterface client, ChatMessage msg) throws RemoteException{
		/*
		 * Add the <ClientInterface, ChatMessage> pair to the ClientList & the ChatMessage to the "universal" messageList.
		 */
		
		if( !isRegistered(client) ){
			clientList.put(client, msg);
			
			messageList.add(msg);
			System.out.println("Registered client " + client.getUsername() +".");
		}
		else{
			System.out.println("Client already registered.");
		}

		sendMessage();

		//updateWatermark();	
	}

	@Override
	public void register(ClientInterface client) throws RemoteException{
	
		/*
		 * Add the <ClientInterface, ChatMessage> pair to the ClientList 
		 * where the ChatMessage is first message that will be associated with the Client,
		 * this ChatMessage is a generic message to announce the Clients precense in the Chat Room. 
		 * & the ChatMessage to the "universal" messageList.
		 */
		
		ChatMessage msg = new ChatMessage("moderator", "Welcome to the Chatroom " + client.getUsername() + "!");

		if( !isRegistered(client) ){

			clientList.put(client, msg);
			messageList.add(0, msg);			
			System.out.println("Registered client " + client.getUsername() +".");
		}
		else{
			System.out.println("Client already registered.");
		}

		sendMessage();

		//updateWatermark();	
	}

	@Override
	public boolean isRegistered(ClientInterface client) throws RemoteException{
		//return clientList.contains(client);
		return clientList.containsKey(client);

	}


}
