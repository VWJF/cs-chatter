package com.b6w7.eece411.Server;

import java.io.*;
import java.rmi.*;

import com.b6w7.eece411.HelloImpl;

public class HelloServer{
	/*
	 * Server program for the "Hello, world!" example.
	 */
	public static void main (String[] args) {
		try {
			Naming.rebind ("SHello", 
					new HelloImpl ("Hello, world!"));
			System.out.println ("HelloServer is ready.");
		} catch (Exception e) {
			System.out.println ("HelloServer failed: " + e);
		}
	}
	
	
}
