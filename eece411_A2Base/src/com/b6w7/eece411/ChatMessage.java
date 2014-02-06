package com.b6w7.eece411;

public class ChatMessage implements ChatMessageInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
	private String username;
	
	public ChatMessage(String username, String message) {
		this.username = username;
		this.message = message;
	}

	//Accessor
	public String getUsername(){
		return this.username;
	}
	
	//Accessor
	public String getReply(){
		return this.message;
	}
	
	/*
	 * Create a String representation of the chat message.
	 * The representation is of the form: "username" :> "message"
	*/
	@Override
	public String message() {
		return username + ":> " + message;
	}

}
