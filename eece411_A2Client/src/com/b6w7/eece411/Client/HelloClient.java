package com.b6w7.eece411.Client;

import java.io.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.b6w7.eece411.HelloInterface;

public class HelloClient{
	/*
	 * Client program for the "Hello, world!" example
	 */
	public static void main (String[] args) {
		try {
            Registry registry = LocateRegistry.getRegistry();
			
            HelloInterface hello = (HelloInterface) 
					registry.lookup ("SHello");
					//Naming.lookup ("//matei.ece.ubc.ca/SHello");
            
			/* ... Now remote calls on hello can be used ... */
			System.out.println (hello.say());
		} 
		catch (Exception e) {
			System.out.println ("HelloClient failed: " + e);
		}
	}

}
