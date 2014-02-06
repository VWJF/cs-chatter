/**
 * 
 */
package com.b6w7.eece411.Client;

import com.matei.eece411.GUI.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

import com.b6w7.eece411.ChatMessage;
import com.b6w7.eece411.ClientInterface;
import com.b6w7.eece411.ServerInterface;

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
		int userID =  (int) (Math.random() * 100);
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
		if(gui == null)
			System.out.println("Gui is null");
		System.out.println("Gui is not null");
		gui.addToTextArea( answer.message() );
	}

	public static GUI gui;
	static MessageQueue _queue;

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
		String registryAddress;
		int registryPort;
		
		// If the command line arguments are missing, then nothing to do
		if ( args.length < 1 || args.length > 2 ) {
			printUsage();
			return;
		}

		// validate registry information from command line parameters 
		// validate host can be found and port is within range.
		// use default registry port if unspecified port.
		try {
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
		
		System.out.println ("HelloClient is starting.  "
				+"Looking for registry at " + registryAddress + " on port " + registryPort);


		ServerInterface server = null;
		ClientInterface client = null;


		// create a shared buffer where the GUI add the messages thet need to 
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


		// hack make sure the GUI instantioation is completed by the GUI thread 
		// before the next call
		while (gui == null)
			Thread.currentThread().yield();

		// calling the GUI method that updates the text area of the GUI
		// NOTE: you might want to call the same method when a new chat message 
		//       arrives
		gui.addToTextArea("RemoteUser:> Sample of displaying remote maessage");


		// The code below serves as an example to show how the shares message 
		// between the GUI and the main thread.
		// You will probably want to replace the code below with code that sits in a loop,  
		// waits for new messages to be entered by the user, and sends them to the 
		// chat server (using an RMI call)
		// 
		// In addition you may want to add code that
		//   * connects to the chat server and provides an object for callbacks (so 
		//     that the server has a way to send messages generated by other users)
		//   * implement the callback object which is called by the server remotely 
		//     and, in turn, updates the local GUI

		try {
			Registry registry = LocateRegistry.getRegistry(registryAddress, registryPort);
			client = new ChatClient();
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}

		try {
			Registry registry = LocateRegistry.getRegistry();

			server = (ServerInterface) 
					registry.lookup ("SHello");
			//Naming.lookup ("//matei.ece.ubc.ca/SHello");
			System.out.println ("ChatClient is ready.");

			server.register( client );
			System.out.println("Client registered");	
		} 
		catch (Exception e) {
			System.out.println ("ServerInterface failed: " + e);
		}



		while (true) {
			String s = null;
			try {
				// wait until the user enters a new chat message
				s = _queue.dequeue();
				/*if ( !server.isRegistered( client ) ){
					server.register( client, new ChatMessage(client.getUsername(), s) );
					System.out.println("Client registered");
				}
				else{ */
					server.postMessage(new ChatMessage(client.getUsername(), s));
					//}
			}
			catch (InterruptedException ie) {
				break;
			}
			catch (Exception e) {
				System.out.println ("ServerInterface failed: " + e);
				e.printStackTrace();
			}

			// update the GUI with the message entered by the user
			// gui.addToTextArea("Me:> " + s); 

			// print it to System.out (or send it to the RMI server)
			System.out.println ("User entered: " + s + " -- now sending it to chat server");
		} // end while loop
	}

}
