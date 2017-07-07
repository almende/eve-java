package com.almende.luis;

import java.io.IOException;
import java.util.Random;

import org.joda.time.DateTime;

import com.almende.eve.agent.Agent;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Optional;
import com.almende.util.TypeUtil;
import com.almende.util.URIUtil;

@Access(AccessType.PUBLIC)
public class HandGameServer extends Agent {

	// Create a string type using TypeUtil. When the type is used a lot it performs better declaring it this way
	private final static TypeUtil<String> STRINGTYPE =  new TypeUtil<String>(){};
	
	public String getHand (){
		
		String handMessage;
		
		// create a random number between 1 and 3
		Random rand = new Random();
		int hand = rand.nextInt(3) + 1;
		
		// codify the number
		if(hand == 1){
			handMessage = "rock";
		}else if(hand == 2){
			handMessage = "scissors";
		}else{
			handMessage = "paper";
		}
		
		System.out.println("handMessage = " + handMessage);
		
		// Just for fun lets use the websocket that player1 opened and schedule a event to request a hand from him
		schedule("scheduleCall",null,DateTime.now().plusSeconds(2));
		
		// return the hand
		return handMessage;
		
	}
	
	public void scheduleCall(){
		try {
			String res = caller.callSync(URIUtil.create("wsclient:player1"), "getHand", null, STRINGTYPE);
			System.out.println("I got from player 1 this result: " + res);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
