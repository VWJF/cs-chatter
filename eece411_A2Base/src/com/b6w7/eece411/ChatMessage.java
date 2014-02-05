package com.b6w7.eece411;

public class ChatMessage implements ChatMessageInterface {

	private String message;
	private String username;
	
	public ChatMessage(String username, String message) {
		// TODO Auto-generated constructor stub
		this.username = username;
		this.message = message;
	}

	public String getUsername(){
		return this.username;
	}
	

	public String getReply(){
		return this.message;
	}
	
	@Override
	public String message() {
		// TODO Auto-generated method stub
		return username + ":> " + message;
	}

}
