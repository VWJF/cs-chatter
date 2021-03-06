package com.b6w7.eece411.Server;

import com.b6w7.eece411.ChatMessage;
import com.b6w7.eece411.ClientInterface;

public class ClientStructure {

	private ClientInterface client;
	private ChatMessage msg;
	
	public ClientStructure(ClientInterface ci, ChatMessage cm) {
		this.client = ci;
		this.msg = cm;
	}

	/*
	 * Accesor
	 * */
	public ClientInterface getClientInterface(){
		return client;
	}

	/*
	 * Accesor
	 * */
	public ChatMessage getChatMessage(){
		return msg;
	}
	
	/*
	 * Mutator
	 */
	public void setChatMessage(ChatMessage update){
		msg = update;
	}

}
