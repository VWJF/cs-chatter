package com.b6w7.eece411.Client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.b6w7.eece411.ChatMessage;
import com.b6w7.eece411.ClientInterface;
import com.b6w7.eece411.ServerInterface;
import com.matei.eece411.GUI.GUI;
import com.matei.eece411.GUI.MessageQueue;

/**
 * Client that connects to {@link ChatServer}
 */
public class ChatClient
implements ClientInterface {
	// static variables
	private static final long DELAY_TO_RECONNECT_MS = 1000;
	private static GUI gui = null;
	private static MessageQueue _queue = null;
	private static Registry registry = null;
	private static String registryAddress = null;
	private static int registryPort = -1;
	public static ClientInterface client = null;
	public static ServerInterface server = null;
	private static Object serverSemaphore = new Object();
	private static Thread mainThread = null;
	private static Thread connectThread = null;

	// member variables
	private String username = null;

	/**
	 * Constructor of a ChatClient 
	 */
	public ChatClient() throws RemoteException {
		//In order to create (semi-)unique user-names for several instances of the client. 
		//Used for testing.
		int userID =  (int) (Math.random() * 100);
		this.username = Integer.toString( (int)userID );
		
//		asyncConnectToServer();
	}

	// Resets the connection from the client to the server.
	// Spawns a thread which continually attempts to connect the client with the server.
	// The thread will keep retrying at 1s intervals until the connection
	// is established.  Until that time, server is set to null.
	// If this reconnection thread is already running, then this method does nothing,
	// as the thread has already been spawned and is currently running.
	private static void asyncConnectToServer(){
		
		// If thread is alive then we are already trying to connect.
		// nothing to do here.
		if (null != connectThread && connectThread.isAlive()) 
			return;

		server = null;
		
		// spawn a new thread
		// this thread sits in a loop, trying to 
		// (1) obtain a ref to the registry,
		// (2) register the chat client in the server.
		// (3) obtain a remote ref to the server, and
		// (4) register client with server.
		// This thread synchronizes on the chat client object,
		// and sleeps 1s between retries
		connectThread = new Thread(new Runnable() {

			@Override
			public void run() { 
				while (null == server) {
					synchronized (ChatClient.serverSemaphore) {
						try {
							registry = LocateRegistry.getRegistry(
									ChatClient.registryAddress, 
									ChatClient.registryPort);
							UnicastRemoteObject.exportObject(client, 0);
							server = (ServerInterface) 
									registry.lookup ("SHello");
							server.register( client );

							gui.addToTextArea("HelloClient successfully connected with server.");
							System.out.println("HelloClient successfully connected with server.");	
							// All 4 of the above succeeded, so we are done.
							return;
							
						//} catch (RemoteException | NotBoundException e) {
						} catch (RemoteException  e) {	
							e.printStackTrace();
							gui.addToTextArea("HelloClient failed to connect with server."
									+"\nRetrying in " + (DELAY_TO_RECONNECT_MS/1000) + " seconds...");
							System.out.println("HelloClient failed to connect with server."
									+"\nRetrying in " + (DELAY_TO_RECONNECT_MS/1000) + " seconds...");
							// At least one of the above 4 failed,
							// set server to null, so that elsewhere
							// in the code we know that server ref is stale
							server = null;
						} catch (NotBoundException e) {
							e.printStackTrace();
							gui.addToTextArea("HelloClient failed to connect with server."
									+"\nRetrying in " + (DELAY_TO_RECONNECT_MS/1000) + " seconds...");
							System.out.println("HelloClient failed to connect with server."
									+"\nRetrying in " + (DELAY_TO_RECONNECT_MS/1000) + " seconds...");
							// At least one of the above 4 failed,
							// set server to null, so that elsewhere
							// in the code we know that server ref is stale
							server = null;
						}
						
					}

					try {
						Thread.sleep(DELAY_TO_RECONNECT_MS);
					} catch (InterruptedException e) {
						// do nothing.  Spurious interrupt signals can occur.
					}
					
					// if main thread has exited, then kill this thread
					if (!mainThread.isAlive())
						return;
				}
			}
		});
		
			connectThread.start();
	}


	@Override
	public String getUsername() throws RemoteException{
		return username;
	}

	@Override
	public void replyToClientGUI(ChatMessage answer) throws RemoteException {
		if(gui == null)
			System.err.println("Gui is null");
		gui.addToTextArea( answer.message() );
	}

    // display usage syntax for running client with GUI
	private static void printUsage() {
		System.out.println("USAGE:\n"
				+ "  java -cp"
				+ " <file.jar>"
				+ " " + ChatClient.class.getCanonicalName() 
				+ " <registry URL>"  
				+ " [<registry port>]");
		System.out.println("EXAMPLE:\n"
				+ "  java -cp"
				+ " A2.jar"
				+ " " + ChatClient.class.getCanonicalName() 
				+ " localhost");
	}

	public static void main(String[] args) {
		String address;
		int port;
		
		// If the command line arguments are missing, then nothing to do
		if ( args.length < 1 || args.length > 2 ) {
			printUsage();
			return;
		}

		// validate registry information from command line parameters 
		// validate host can be found and port is within range.
		// use default registry port if unspecified port.
		try {
			address = args[0];
			InetAddress.getByName(args[0]);
			if (args.length == 1)
				port = Registry.REGISTRY_PORT;
			else
				port = Integer.parseInt(args[1]);
			if (port < 1024 || port > 65535)
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
		
		System.out.println ("HelloClient is starting.  "
				+"Looking for registry at " + address + " on port " + port);

		// create a shared buffer where the GUI add the messages that need to 
		// be sent out by the main thread.  The main thread stays in a loop 
		// and when a new message shows up in the buffer it sends it out 
		// to the chat server (using RMI)
		_queue = new MessageQueue();

		// instantiate the GUI - in a new thread
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui = GUI.createAndShowGUI(_queue);  
			}
		});

		// hack make sure the GUI instantiation is completed by the GUI thread 
		// before the next call
		while (gui == null)
			Thread.yield();

		// calling the GUI method that updates the text area of the GUI
		gui.addToTextArea("HelloClient is starting.  "
				+"Looking for registry at " + address + " on port " + port);

		// store a reference to the main thread so that
		// other threads can poll this reference to see
		// if main thread is still running
		mainThread = Thread.currentThread();

		ChatClient.registryAddress = address;
		ChatClient.registryPort = port;
		
		try {
			client = new ChatClient();			
			asyncConnectToServer();

		} catch (RemoteException e1) {
			// Not expected, close application
			e1.printStackTrace();
			System.err.println("Unexpected RemoteException.  Closing.");
			return;
		}

		while (true) {
			String s = null;
			try {
				// wait until the user enters a new chat message
				s = _queue.dequeue();
				if (null != server) {
					server.postMessage(new ChatMessage(client.getUsername(), s));
				} else {
					//handle to server is lost.
					throw new RemoteException("No connection to server present.");
				}
				
			} catch (RemoteException e) {
				
				gui.addToTextArea("Server not responsive.  Message \"" + s + "\" dropped.");
				System.out.println("Server not responsive.  Message \"" + s + "\" dropped. : ");
				System.out.println(e.getLocalizedMessage());
				
				asyncConnectToServer();

			} catch (InterruptedException ie) {
				break;
			}

			// print it to System.out (or send it to the RMI server)
			System.out.println ("User entered: " + s + " -- now sending it to chat server");
		}
	}
}
